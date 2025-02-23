import mongoose, { Schema, Document } from "mongoose";

export interface IHousehold extends Document {
    name: string;
    managerId: mongoose.Types.ObjectId;
    users: mongoose.Types.ObjectId[];
    pets: mongoose.Types.ObjectId[];
}

const HouseholdSchema = new Schema<IHousehold>({
    name: { type: String, required: true },
    managerId: { type: Schema.Types.ObjectId, ref: "User", required: true },
    users: [{ type: Schema.Types.ObjectId, ref: "User" }],
    pets: [{ type: Schema.Types.ObjectId, ref: "Pet" }],
});

export default mongoose.model<IHousehold>("Household", HouseholdSchema);