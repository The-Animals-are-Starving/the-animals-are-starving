import mongoose, { Document, Schema } from "mongoose";

export type UserRole = "restricted" | "normal" | "manager";

export interface IUser extends Document {
    email: string;
    name: string;
    householdId?: string;
    role: UserRole;
}

const UserSchema = new Schema<IUser>({
    email: { type: String, required: true, unique: true },
    name: { type: String, required: true },
    householdId: { type: String },
    role: { type: String, enum: ["restricted", "normal", "manager"], default: "normal" }
});

export default mongoose.model<IUser>("User", UserSchema);