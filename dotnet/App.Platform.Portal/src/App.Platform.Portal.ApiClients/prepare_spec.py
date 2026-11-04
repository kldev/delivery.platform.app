"""Adjusts an OpenAPI document fetched from the gateway before Kiota reads it (run by generate.sh).

Each change describes what the service really sends, where its generator describes it differently:

- "*/*" responses (springdoc, for controllers without "produces"): application/json - the Spring services answer
  with JSON, and Kiota types only structured media types (it would return a Stream).
- servers: removed - the base address is the gateway's, set by the portal's configuration (Gateway:BaseUrl).
- "type": ["number", "string"] (ASP.NET Core writes it for decimal/double): a plain number - the payment service sends
  numbers, and the pair would become a composed wrapper type in C#.
- JsonNode / ObjectNode (Quarkus describes Jackson's tree by its getters): a free-form object.
- ProblemDetail (Spring): the services add the RFC 9457 extension members "code" and "errors" at the top level, which
  springdoc describes as one "properties" map; described as they are sent.

Usage: python3 prepare_spec.py openapi/<service>.json   (rewrites the file in place)
"""

import json
import sys

FREE_FORM_OBJECT = {"type": "object", "additionalProperties": True}


def collapse_number_or_string(node):
    if isinstance(node, dict):
        kind = node.get("type")
        if isinstance(kind, list) and "number" in kind and "string" in kind:
            rest = [k for k in kind if k not in ("number", "string")]
            node["type"] = ["number", *rest] if rest else "number"
            node.pop("pattern", None)
        for value in node.values():
            collapse_number_or_string(value)
    elif isinstance(node, list):
        for value in node:
            collapse_number_or_string(value)


def inline_scalars(node, scalars):
    """$ref to a scalar schema (UUID, Instant, LocalDate) -> the scalar itself; anyOf [X, null] -> nullable X."""
    if isinstance(node, list):
        return [inline_scalars(value, scalars) for value in node]
    if not isinstance(node, dict):
        return node
    ref = node.get("$ref", "")
    name = ref.rsplit("/", 1)[-1]
    if ref.startswith("#/components/schemas/") and name in scalars:
        return dict(scalars[name])
    node = {key: inline_scalars(value, scalars) for key, value in node.items()}
    for key in ("anyOf", "oneOf"):
        variants = node.get(key)
        if isinstance(variants, list) and len(variants) == 2 and {"type": "null"} in variants:
            other = next(v for v in variants if v != {"type": "null"})
            if "type" in other and isinstance(other["type"], str):
                del node[key]
                node.update(other)
                node["type"] = [other["type"], "null"]
    return node


def main(path):
    with open(path, encoding="utf-8") as f:
        doc = json.load(f)

    doc.pop("servers", None)
    collapse_number_or_string(doc)
    for operations in doc.get("paths", {}).values():
        for operation in operations.values():
            for response in operation.get("responses", {}).values():
                content = response.get("content", {})
                if "*/*" in content:
                    content["application/json"] = content.pop("*/*")

    schemas = doc.get("components", {}).get("schemas", {})
    scalars = {
        name: schema
        for name, schema in schemas.items()
        if schema.get("type") in ("string", "integer", "number", "boolean") and "enum" not in schema
    }
    for name in scalars:
        del schemas[name]
    doc = inline_scalars(doc, scalars)
    schemas = doc.get("components", {}).get("schemas", {})
    for name in ("JsonNode", "ObjectNode"):
        if name in schemas:
            schemas[name] = dict(FREE_FORM_OBJECT)
    schemas.pop("JsonNodeType", None)

    problem = schemas.get("ProblemDetail")
    if problem is not None:
        properties = problem.setdefault("properties", {})
        properties.pop("properties", None)
        properties["code"] = {"type": "string", "description": "Stable, machine-readable reason, e.g. delivery.not-found"}
        properties["errors"] = {
            "type": "array",
            "description": "Invalid fields of the request",
            "items": {
                "type": "object",
                "properties": {"field": {"type": "string"}, "message": {"type": "string"}},
            },
        }

    with open(path, "w", encoding="utf-8") as f:
        json.dump(doc, f, indent=2, ensure_ascii=False)
        f.write("\n")


if __name__ == "__main__":
    main(sys.argv[1])
