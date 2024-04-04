import MessageResponse from "./models/message_response";
import "dotenv/config";
import { logger } from "firebase-functions/v2";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import { setGlobalOptions } from "firebase-functions/v2/options";
import CallMessageResponse from "./models/call_message_response";
import { getFirestore } from "firebase-admin/firestore";
import ConversationResponse from "./models/conversation_response";
import { NotificationType } from "./models/notification_type";

const app = admin.initializeApp({
  credential: admin.credential.cert({
    privateKey: process.env.PRIVATE_KEY,
    projectId: process.env.PROJECT_ID,
    clientEmail: process.env.CLIENT_EMAIL,
  }),
});
setGlobalOptions({ maxInstances: 10 });

export const messageNotification = onDocumentCreated(
  { document: "messages/{messageId}", region: "asia-southeast2" },
  async (snapshot) => {
    const message: MessageResponse = snapshot.data?.data() as MessageResponse;
    const messageJson = JSON.stringify(message);
    logger.log(`createdMessage : ${messageJson}`);
    try {
      const isSentResult = await admin
        .messaging()
        .sendToTopic(message.conversationId, {
          data: {
            notificationType: NotificationType.Message,
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

export const callNotification = onDocumentCreated(
  { document: "ongoing_call/{conversationId}", region: "asia-southeast2" },
  async (snapshot) => {
    const ongoingCallResponseMessage: CallMessageResponse =
      snapshot.data?.data() as CallMessageResponse;
    const ongoingCallResponseMessageJson = JSON.stringify(
      ongoingCallResponseMessage,
    );
    logger.log(`createdMessage : ${ongoingCallResponseMessageJson}`);

    const conversationResponse = await getFirestore(app)
      .collection("conversations")
      .where("id", "==", ongoingCallResponseMessage.conversationId)
      .get();

    const conversation = conversationResponse.docs.map(
      (conversation) => conversation.data() as ConversationResponse,
    )[0];

    const conversationJson = JSON.stringify(conversation);
    logger.log(`conversation: ${conversationJson}`);

    try {
      const isSentResult = await admin
        .messaging()
        .sendToTopic(ongoingCallResponseMessage.conversationId, {
          data: {
            notificationType: NotificationType.Call,
            callInitiatorId: ongoingCallResponseMessage.callInitiatorId,
            conversationId: ongoingCallResponseMessage.conversationId,
            sessionDescription: ongoingCallResponseMessage.sessionDescription,
          },
          notification: {
            title: `Incoming call from ${conversation.conversationName}`,
            body: "Incoming call",
          },
        });
      logger.log(
        `message sendToTopic: ${ongoingCallResponseMessage.conversationId} with messageId: ${isSentResult.messageId}`,
      );
    } catch (error) {
      logger.error(error);
    }
  },
);
