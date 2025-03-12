import mongoose, { Document, Schema, Types } from "mongoose";

export interface ILog extends Document {
    petName: String;
    amount: number;
    householdId?: Types.ObjectId;
    timestamp: Date;
    userName: String;
}

const LogSchema = new Schema<ILog>({
    petName: { type: String, ref: "Pet", required: true },
    userName: { type: String, ref: "User", required: true },
    amount: { type: Number, required: true },
    householdId: { type: Schema.Types.ObjectId, ref: "Household" },
    timestamp: { type: Date, default: Date.now }
});

export default mongoose.model<ILog>("Log", LogSchema);
