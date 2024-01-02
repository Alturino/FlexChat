export default interface MessageResponse {
  id: string;
  userId: string;
  conversationId: string;
  conversationMemberId: string;
  messageBody: string;
  senderName: string;
  senderPhotoUrl: string;
  createdAt: Date;
  deletedAt: Date;
}
