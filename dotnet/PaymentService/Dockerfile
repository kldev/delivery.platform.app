FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /src

COPY Delivery.Platform.slnx .

COPY Delivery.Platform.Domain/Delivery.Platform.Domain.csproj Delivery.Platform.Domain/
COPY Delivery.Platform.Infrastructure/Delivery.Platform.Infrastructure.csproj Delivery.Platform.Infrastructure/
COPY Delivery.Platform.PaymentService/Delivery.Platform.PaymentService.csproj Delivery.Platform.PaymentService/

RUN dotnet restore Delivery.Platform.PaymentService/Delivery.Platform.PaymentService.csproj  --ignore-failed-sources

COPY Delivery.Platform.Domain/ Delivery.Platform.Domain/
COPY Delivery.Platform.Infrastructure/ Delivery.Platform.Infrastructure/
COPY Delivery.Platform.PaymentService/ Delivery.Platform.PaymentService/

RUN dotnet publish \
    Delivery.Platform.PaymentService/Delivery.Platform.PaymentService.csproj \
    -c Release \
    -o /app/publish \
    --no-restore \
    /p:UseAppHost=false


FROM runtime

WORKDIR /app

COPY --from=build /app/publish .

ENTRYPOINT ["dotnet", "Delivery.Platform.PaymentService.dll"]
