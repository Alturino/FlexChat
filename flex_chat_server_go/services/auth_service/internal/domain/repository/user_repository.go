package repository

import (
	"context"
	"database/sql"

	"github.com/Alturino/FlexChat/flex_chat_server_go/services/auth_service/internal/domain/model"
)

type UserRepository interface {
	FindByEmail(ctx context.Context, email string) (user model.User, err error)
	FindById(ctx context.Context, userId string) (user model.User, err error)
	FindByUsername(ctx context.Context, username string) (user model.User, err error)
	Insert(ctx context.Context, user model.User) (userId string, err error)
	UpdatePassword(ctx context.Context, userId string, password string) (id string, err error)
}

func NewUserRepository(db *sql.DB) UserRepository {
	return &userRepositoryImpl{db: db}
}

type userRepositoryImpl struct {
	db *sql.DB
}

func (r userRepositoryImpl) FindByEmail(
	ctx context.Context,
	email string,
) (user model.User, err error) {
	row := r.db.QueryRowContext(
		ctx,
		"select id, username, photo_profile_url, email, password, phone_number, is_private, created_at, updated_at, deleted_at, from user where email = $1",
		email,
	)
	err = row.Scan(
		&user.ID,
		&user.Username,
		&user.PhotoProfileUrl,
		&user.Email,
		&user.Password,
		&user.PhoneNumber,
		&user.IsPrivate,
		&user.CreatedAt,
		&user.UpdatedAt,
		&user.DeletedAt,
	)
	if err != nil {
		return user, err
	}
	return user, nil
}

func (r userRepositoryImpl) FindById(
	ctx context.Context,
	userId string,
) (user model.User, err error) {
	row := r.db.QueryRowContext(
		ctx,
		"select id, username, photo_profile_url, email, password, phone_number, is_private, created_at, updated_at, deleted_at, from user where id = $1",
		userId,
	)
	err = row.Scan(
		&user.ID,
		&user.Username,
		&user.PhotoProfileUrl,
		&user.Email,
		&user.Password,
		&user.PhoneNumber,
		&user.IsPrivate,
		&user.CreatedAt,
		&user.UpdatedAt,
		&user.DeletedAt,
	)
	if err != nil {
		return user, err
	}
	return user, nil
}

func (r userRepositoryImpl) FindByUsername(
	ctx context.Context,
	username string,
) (user model.User, err error) {
	row := r.db.QueryRowContext(
		ctx,
		"select id, username, photo_profile_url, email, password, phone_number, is_private, created_at, updated_at, deleted_at, from user where username = $1",
		username,
	)
	err = row.Scan(
		&user.ID,
		&user.Username,
		&user.PhotoProfileUrl,
		&user.Email,
		&user.Password,
		&user.PhoneNumber,
		&user.IsPrivate,
		&user.CreatedAt,
		&user.UpdatedAt,
		&user.DeletedAt,
	)
	if err != nil {
		return user, err
	}
	return user, nil
}

func (r userRepositoryImpl) Insert(
	ctx context.Context,
	user model.User,
) (userId string, err error) {
	row := r.db.QueryRowContext(
		ctx,
		"INSERT INTO users(id, username, photo_profile_url, email, password, phone_number, is_private, created_at, updated_at, deleted_at) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNS id",
		&user.ID,
		&user.Username,
		&user.PhotoProfileUrl,
		&user.Email,
		&user.Password,
		&user.PhoneNumber,
		&user.IsPrivate,
		&user.CreatedAt,
		&user.UpdatedAt,
		&user.DeletedAt,
	)
	err = row.Scan(&userId)
	if err != nil {
		return "", err
	}
	return userId, nil
}

func (r userRepositoryImpl) UpdatePassword(
	ctx context.Context,
	userId string,
	password string,
) (id string, err error) {
	row := r.db.QueryRowContext(
		ctx,
		"select id, username, photo_profile_url, email, password, phone_number, is_private, created_at, updated_at, deleted_at, from user where id = $1",
		userId,
	)
}
