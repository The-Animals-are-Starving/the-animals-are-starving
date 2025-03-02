import mongoose, { Document, Schema, Types } from "mongoose";

export interface IPet extends Document { //note ID removed 
    name: string;
    householdId?: Types.ObjectId;
    feedingTime: string; // changed to string, this will cause problems elsewhere
    fed: boolean;
    lastTimeFed?: Date;
}

const PetSchema = new Schema<IPet>({
    name: { type: String, required: true },
    householdId: { type: Schema.Types.ObjectId, ref: "Household" },
    feedingTime: { type: String, required: true },
    fed: { type: Boolean, default: false },
    lastTimeFed: { type: Date }
});

export default mongoose.model<IPet>("Pet", PetSchema);
