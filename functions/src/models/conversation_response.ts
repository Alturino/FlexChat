export default interface ConversationResponse {
  id: string;
  userIds: string[];
  conversationMemberIds: string[];
  attachmentIds: string[];
  messageIds: string[];
  conversationName: string;
  slug: string;
  group: boolean;
  createdAt: Date;
  deletedAt?: Date;
}
