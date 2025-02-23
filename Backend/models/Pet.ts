import mongoose, { Document, Schema, Types } from "mongoose";

export interface IPet extends Document {
    petId: number;
    name: string;
    householdId?: Types.ObjectId;
    fed: boolean;
}

const PetSchema = new Schema<IPet>({
    petId: { type: Number, required: true, unique: true },
    name: { type: String, required: true },
    householdId: { type: Schema.Types.ObjectId, ref: "Household" },
    fed: { type: Boolean, default: false }
});

export default mongoose.model<IPet>("Pet", PetSchema);
