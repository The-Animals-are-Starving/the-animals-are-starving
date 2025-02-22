import { Int32, IntegerType } from "mongodb";
import mongoose, { Document, Schema } from "mongoose";

export interface IPet extends Document {
    petId: IntegerType;
    name: string;
    householdId?: string;
}

const UserSchema = new Schema<IPet>({
    petId: { type: Int32, required: true },
    name: { type: String, required: true },
    householdId: { type: String }
});

export default mongoose.model<IPet>("User", UserSchema);