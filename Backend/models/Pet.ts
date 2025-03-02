import mongoose, { Document, Schema, Types } from "mongoose";

export interface IPet extends Document {
    petId: number;
    name: string;
    householdId?: Types.ObjectId;
    feedingTime: Date;
    fed: boolean;
    lastTimeFed?: Date;
}

const PetSchema = new Schema<IPet>({
    petId: { type: Number, required: true, unique: true },
    name: { type: String, required: true },
    householdId: { type: Schema.Types.ObjectId, ref: "Household" },
    feedingTime: { type: Date, required: true },
    fed: { type: Boolean, default: false },
    lastTimeFed: { type: Date }
});

export default mongoose.model<IPet>("Pet", PetSchema);
