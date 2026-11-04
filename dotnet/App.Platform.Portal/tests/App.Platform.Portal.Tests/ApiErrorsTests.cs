using App.Platform.Portal.Platform;
using Microsoft.Kiota.Abstractions;
using DeliveryProblem = App.Platform.Portal.ApiClients.Delivery.Models.ProblemDetail;
using DeliveryProblemError = App.Platform.Portal.ApiClients.Delivery.Models.ProblemDetail_errors;

namespace App.Platform.Portal.Tests;

public class ApiErrorsTests
{
    [Fact]
    public void A_known_rule_code_is_told_in_the_portal_words()
    {
        var problem = new DeliveryProblem
        {
            Code = "driver.none-available",
            Detail = "There is no driver to assign",
            ResponseStatusCode = 422,
        };

        ApiErrors
            .Describe(problem)
            .Message.ShouldBe("Nie ma wolnego kierowcy. Dodaj kierowcę albo spróbuj później.");
    }

    [Fact]
    public void Invalid_fields_are_returned_per_field()
    {
        var problem = new DeliveryProblem
        {
            Code = "validation",
            ResponseStatusCode = 400,
            Errors =
            [
                new DeliveryProblemError
                {
                    Field = "distanceKm",
                    Message = "must be greater than or equal to 0.1",
                },
            ],
        };

        var failure = ApiErrors.Describe(problem);

        failure.Field("distanceKm").ShouldBe("must be greater than or equal to 0.1");
        failure.Message.ShouldBe("Popraw zaznaczone pola.");
    }

    [Fact]
    public void An_unknown_code_falls_back_to_the_service_detail()
    {
        var problem = new DeliveryProblem
        {
            Code = "delivery.something-new",
            Detail = "Something new happened",
            ResponseStatusCode = 409,
        };

        ApiErrors.Describe(problem).Message.ShouldBe("Something new happened");
    }

    [Fact]
    public void An_unreachable_gateway_says_how_to_start_the_stack() =>
        ApiErrors
            .Describe(new HttpRequestException("Connection refused"))
            .Message.ShouldContain("docker compose up -d");

    [Fact]
    public void A_bad_gateway_answer_names_the_service_as_unavailable() =>
        ApiErrors
            .Describe(new ApiException { ResponseStatusCode = 502 })
            .Message.ShouldStartWith("Serwis jest teraz niedostępny");
}
