import mongoose, { Document, Schema, Types } from "mongoose";

export interface ILog extends Document {
    petId: Types.ObjectId;
    amount: number;
    householdId?: Types.ObjectId;
    timestamp: Date;
    userId: Types.ObjectId;
}

const LogSchema = new Schema<ILog>({
    petId: { type: Schema.Types.ObjectId, ref: "Pet", required: true },
    userId: { type: Schema.Types.ObjectId, ref: "User", required: true },
    amount: { type: Number, required: true },
    householdId: { type: Schema.Types.ObjectId, ref: "Household" },
    timestamp: { type: Date, default: Date.now }
});

export default mongoose.model<ILog>("Log", LogSchema);
