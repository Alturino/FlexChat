export default interface UserResponse {
  id: string;
  ongoing_call_id: string;
  username: string;
  photo_profile_url: string;
  created_at: Date;
  deleted_at: Date;
  phone_number: string;
  isPrivate: boolean;
  email: string;
  isOnline: boolean;
}
