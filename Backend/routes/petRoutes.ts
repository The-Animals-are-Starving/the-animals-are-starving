import express from "express";
import { addPet, getPetsByHousehold, updatePetFeedingStatus, removePet } from "../controllers/petController";

const router = express.Router();

router.post("/", addPet);
router.get("/household/:householdId", getPetsByHousehold);
router.patch("/:petId/feed", updatePetFeedingStatus);
router.delete("/:petId", removePet);

export default router;