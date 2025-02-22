import mongoose, { Document, Schema } from "mongoose";

export interface IUser extends Document {
    email: string;
    name: string;
    householdId?: string;
}

const UserSchema = new Schema<IUser>({
    email: { type: String, required: true, unique: true },
    name: { type: String, required: true },
    householdId: { type: String }
});

export default mongoose.model<IUser>("User", UserSchema);