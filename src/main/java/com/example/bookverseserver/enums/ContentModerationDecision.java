package com.example.bookverseserver.enums;

/**
 * Content moderation decision to take on UGC.
 * Stolen from chefkix pattern, adapted for Bookverse.
 * 
 * NOT to be confused with ModerationAction entity (entity/Moderation/ModerationAction.java)
 * which is an audit trail for moderator actions on flagged content.
 */
public enum ContentModerationDecision {
    APPROVE,  // Content is clean, allow submission
    FLAG,     // Uncertain, needs human review (still allow but queue)
    BLOCK     // Content violates policies, reject submission
}
