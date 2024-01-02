import * as admin from "firebase-admin";
import {initializeApp} from "firebase-admin/app"
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import MessageResponse from "./models/message_response";
import { MessagingPayload } from "firebase-admin/lib/messaging/messaging-api";
initializeApp();

export const messageNotification = onDocumentCreated("messages/{messageId}", async (snapshot) => {
  const message: MessageResponse = snapshot.data?.data() as MessageResponse;
  console.log(`${message}`);
  const notificationPayload: MessagingPayload = {
    notification: {
      title: message.senderName,
      body: message.messageBody,
      icon: message.senderPhotoUrl,
    },
  };
  admin.messaging().sendToTopic(message.conversationId, notificationPayload);
});
export GOOGLE_APPLICATION_CREDENTIALS="/home/onirutla/Downloads/flexchat_firebase_app_engine_default_service_account_key.json"
