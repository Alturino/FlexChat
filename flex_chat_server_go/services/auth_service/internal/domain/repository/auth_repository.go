package repository

import (
	"context"
	"database/sql"

	"github.com/Alturino/FlexChat/flex_chat_server_go/auth_service/internal/delivery/model"
)

type AuthRepository struct {
	db *sql.DB
	model.UnimplementedGrpcAuthServiceServer
}

func (r AuthRepository) Login(
	ctx context.Context,
	req *model.LoginRequest,
) (*model.LoginResponse, error) {
	return nil, error
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
