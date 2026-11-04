# syntax=docker/dockerfile:1
# The platform portal (Blazor, Interactive Server). Build context: this folder (dotnet/App.Platform.Portal).
# The Kiota clients are committed (src/App.Platform.Portal.ApiClients/Generated), so the build needs no running stack.

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY global.json nuget.config Directory.Build.props Directory.Packages.props ./
COPY src/App.Platform.Portal.ApiClients/App.Platform.Portal.ApiClients.csproj src/App.Platform.Portal.ApiClients/
COPY src/App.Platform.Portal/App.Platform.Portal.csproj src/App.Platform.Portal/
RUN dotnet restore src/App.Platform.Portal/App.Platform.Portal.csproj
COPY src/ src/
RUN dotnet publish src/App.Platform.Portal/App.Platform.Portal.csproj -c Release -o /out --no-restore -p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS app
WORKDIR /app
ENV ASPNETCORE_HTTP_PORTS=8080 \
    ASPNETCORE_ENVIRONMENT=Production
COPY --from=build /out .
USER $APP_UID
EXPOSE 8080
# No curl in the image: bash's /dev/tcp asks /health and expects 200.
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /health HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && head -n1 <&3 | grep -q ' 200 '"]
ENTRYPOINT ["dotnet", "App.Platform.Portal.dll"]
