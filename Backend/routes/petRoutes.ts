import express from "express";
import { addPet, getPetsByHousehold, updatePetFeedingStatus, removePet } from "../controllers/petController";

const router = express.Router();

router.post("/", addPet);
router.get("/:householdId", getPetsByHousehold);
router.patch("/:petName/feed", updatePetFeedingStatus);
router.delete("/:petName", removePet);

export default router;