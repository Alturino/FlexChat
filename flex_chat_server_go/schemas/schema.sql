-- SQL dump generated using DBML (dbml.dbdiagram.io)
-- Database: PostgreSQL
-- Generated at: 2024-03-31T03:53:34.701Z

CREATE TABLE "users" (
  "id" uuid PRIMARY KEY,
  "username" varchar,
  "photo_profile_url" varchar,
  "created_at" timestamp,
  "deleted_at" timestamp,
  "phone_number" varchar,
  "private" boolean,
  "email" varchar,
  "is_online" boolean
);

CREATE TABLE "conversation_members" (
  "id" uuid PRIMARY KEY,
  "user_id" integer,
  "conversation_id" integer,
  "username" varchar,
  "photo_profile_url" varchar,
  "joined_at" timestamp,
  "left_at" timestamp
);

CREATE TABLE "conversations" (
  "id" uuid PRIMARY KEY,
  "name" varchar,
  "is_group" boolean,
  "created_at" timestamp,
  "deleted_at" timestamp
);

CREATE TABLE "messages" (
  "id" uuid PRIMARY KEY,
  "user_id" integer,
  "conversation_id" integer,
  "conversation_member_id" integer,
  "sender_name" varchar,
  "message_body" varchar,
  "created_at" timestamp,
  "deleted_at" timestamp
);

CREATE TABLE "attachments" (
  "id" uuid PRIMARY KEY,
  "conversation_id" integer,
  "user_id" integer,
  "message_id" integer,
  "url" varchar,
  "created_at" timestamp,
  "deleted_at" timestamp
);

CREATE TABLE "unread_messages" (
  "id" uuid PRIMARY KEY,
  "conversation_member_id" integer,
  "message_id" integer
);

CREATE TABLE "friends" (
  "id" uuid PRIMARY KEY,
  "user_id" integer,
  "with_user_id" integer
);

ALTER TABLE "conversation_members" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "conversation_members" ADD FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id");

ALTER TABLE "messages" ADD FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id");

ALTER TABLE "attachments" ADD FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id");

ALTER TABLE "messages" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "attachments" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "attachments" ADD FOREIGN KEY ("message_id") REFERENCES "messages" ("id");

ALTER TABLE "unread_messages" ADD FOREIGN KEY ("conversation_member_id") REFERENCES "conversation_members" ("id");

ALTER TABLE "messages" ADD FOREIGN KEY ("conversation_member_id") REFERENCES "conversation_members" ("id");

ALTER TABLE "unread_messages" ADD FOREIGN KEY ("message_id") REFERENCES "messages" ("id");

ALTER TABLE "friends" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "friends" ADD FOREIGN KEY ("with_user_id") REFERENCES "users" ("id");
