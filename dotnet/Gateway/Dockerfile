FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /src

COPY DeliveryGateway.slnx .

COPY Delivery.Gateway.Web/Delivery.Gateway.Web.csproj Delivery.Gateway.Web/
RUN dotnet restore Delivery.Gateway.Web/Delivery.Gateway.Web.csproj  --ignore-failed-sources

COPY Delivery.Gateway.Web/ Delivery.Gateway.Web/

RUN dotnet publish \
    Delivery.Gateway.Web/Delivery.Gateway.Web.csproj \
    -c Release \
    -o /app/publish \
    --no-restore \
    /p:UseAppHost=false


FROM runtime

WORKDIR /app

COPY --from=build /app/publish .

ENTRYPOINT ["dotnet", "Delivery.Gateway.Web.dll"]
