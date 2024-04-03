package secret

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
)

const (
	APPLICATION_NAME          = "FlexChatServer"
	LOGIN_EXPIRATION_DURATION = time.Duration(48) * time.Hour
	JWT_SIGNATURE_KEY         = "jwt_signature_key"
)

var JWT_SIGNING_METHOD = jwt.SigningMethodHS256
