# Environment Setup

This project requires several environment variables to be set for OAuth2 authentication and other services. Follow these steps to set up your development environment:

1. Create a `.env` file in the project root directory
2. Add the following environment variables with your own values:

```env
# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Naver OAuth2
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_ADMIN_KEY=your_kakao_admin_key
KAKAO_TEMPLATE_ID=your_kakao_template_id

# Naver Cloud SMS
NAVER_CLOUD_ACCESS_KEY=your_naver_cloud_access_key
NAVER_CLOUD_SECRET_KEY=your_naver_cloud_secret_key
NAVER_CLOUD_SERVICE_ID=your_naver_cloud_service_id
NAVER_CLOUD_SENDER_PHONE=your_naver_cloud_sender_phone

# JWT
JWT_SECRET_KEY=your_jwt_secret_key
```

3. Copy `src/main/resources/application.yml.example` to `src/main/resources/application.yml`
4. Copy `src/test/resources/application.yml.example` to `src/test/resources/application.yml`

**Important: Never commit your `.env` file or the actual `application.yml` files to version control. They are already added to `.gitignore`.**

## Getting OAuth2 Credentials

### Google OAuth2
1. Go to the [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select an existing one
3. Enable the OAuth2 API
4. Create OAuth2 credentials (Client ID and Client Secret)
5. Add authorized redirect URIs (e.g., `http://localhost:8080/login/oauth2/code/google` for local development)

### Naver OAuth2
1. Go to the [Naver Developers](https://developers.naver.com/main/)
2. Create a new application
3. Get your Client ID and Client Secret
4. Add callback URL (e.g., `http://localhost:8080/login/oauth2/code/naver` for local development)

### Kakao OAuth2
1. Go to the [Kakao Developers](https://developers.kakao.com)
2. Create a new application
3. Get your Client ID and Client Secret
4. Add redirect URI (e.g., `http://localhost:8080/login/oauth2/code/kakao` for local development)

## Security Notes
- Keep your credentials secure and never share them
- Use different credentials for development and production environments
- Regularly rotate your secrets
- Consider using a secrets management service in production 