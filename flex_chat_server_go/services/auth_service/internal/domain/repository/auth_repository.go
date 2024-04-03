package repository

import (
	"context"
	"database/sql"
	"errors"
	"log"
	"time"

	jwt "github.com/golang-jwt/jwt/v5"
	"github.com/redis/go-redis/v9"
	"golang.org/x/crypto/bcrypt"

	"github.com/Alturino/FlexChat/flex_chat_server_go/services/auth_service/common/secret"
	"github.com/Alturino/FlexChat/flex_chat_server_go/services/auth_service/internal/delivery/model"
)

type AuthRepository struct {
	db    *sql.DB
	redis *redis.Client
	model.UnimplementedGrpcAuthServiceServer
}

func (r AuthRepository) Login(
	ctx context.Context,
	req *model.LoginRequest,
) (res *model.LoginResponse, err error) {
	row := r.db.QueryRowContext(ctx, "SELECT email, password FROM user WHERE email = $1", req.Email)

	user := &model.User{}
	err = row.Scan(&user.Email, &user.Password)
	if err != nil || errors.Is(err, sql.ErrNoRows) {
		log.Printf("login: query user with email: %s is %s", req.Email, err.Error())
		return res, errors.New("User have not been registered yet")
	}

	hashed, err := bcrypt.GenerateFromPassword([]byte(req.Password), 14)
	if err != nil {
		log.Printf("login: hashing password is failed with error: %s", err.Error())
		return nil, errors.New("Failed to hash password")
	}

	err = bcrypt.CompareHashAndPassword(hashed, []byte(user.Password))
	if err != nil {
		log.Print(
			"login: hashed password from request is not the same from hashed password from database",
		)
		return nil, errors.New("Password is not valid")
	}

	// TODO: generate jwt
	expiry := time.Now().Add(secret.LOGIN_EXPIRATION_DURATION)
	claims := &jwt.RegisteredClaims{
		ExpiresAt: jwt.NewNumericDate(expiry),
		Issuer:    secret.APPLICATION_NAME,
	}
	token := jwt.NewWithClaims(secret.JWT_SIGNING_METHOD, claims)
	ss, err := token.SignedString(secret.JWT_SIGNATURE_KEY)
	if err != nil {
		log.Printf("login: failed to signed jwt: %s", err.Error())
		return nil, err
	}

	r.redis.HSet(ctx)

	return &model.LoginResponse{}, nil

	// TODO: create session save it to redis
	// TODO: return session
}

func (r AuthRepository) Register(
	ctx context.Context,
	req *RegisterRequest,
) (*model.RegisterResponse, error) {
	return nil, error
}

func (r AuthRepository) Session(
	ctx context.Context,
	req *SessionRequest,
) (*SessionResponse, error) {
	return nil, error
}
