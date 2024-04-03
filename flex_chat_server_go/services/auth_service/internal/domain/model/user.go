package model

import (
	"time"
)

type User struct {
	ID              string
	Username        string
	PhotoProfileUrl string
	Email           string
	Password        string
	PhoneNumber     string
	IsPrivate       bool
	CreatedAt       time.Time
	UpdatedAt       time.Time
	DeletedAt       time.Time
}
