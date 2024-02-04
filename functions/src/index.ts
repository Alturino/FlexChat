import MessageResponse from "./models/message_response";
import "dotenv/config";
import {logger} from "firebase-functions";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import {setGlobalOptions} from "firebase-functions/v2/options";

admin.initializeApp({
  credential: admin.credential.cert({
    privateKey: process.env.PRIVATE_KEY,
    projectId: process.env.PROJECT_ID,
    clientEmail: process.env.CLIENT_EMAIL,
  }),
});
setGlobalOptions({maxInstances: 10});

export const messageNotification = onDocumentCreated(
  {document: "messages/{messageId}", region: "asia-southeast-2"},
  async (snapshot) => {
    const message: MessageResponse = snapshot.data?.data() as MessageResponse;
    const messageJson = JSON.stringify(message);
    logger.log(`createdMessage : ${messageJson}`);
    try {
      const isSentResult = await admin
        .messaging()
        .sendToTopic(message.conversationId, {
          data: {
            userId: message.userId,
            conversationId: message.conversationId,
          },
          notification: {
            title: message.senderName,
            body: message.messageBody,
            icon: message.senderPhotoUrl,
          },
        });
      logger.log(
        `message sendToTopic: ${message.conversationId} with messageId: ${isSentResult.messageId}`,
      );
    } catch (error) {
      logger.error(error);
    }
  },
);
