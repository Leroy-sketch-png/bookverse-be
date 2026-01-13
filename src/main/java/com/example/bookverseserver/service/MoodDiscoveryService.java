package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.AI.MoodDiscoveryRequest;
import com.example.bookverseserver.dto.response.AI.MoodDiscoveryResponse;
import com.example.bookverseserver.dto.response.AI.MoodDiscoveryResponse.MoodRecommendation;
import com.example.bookverseserver.dto.response.AI.MoodDiscoveryResponse.RelatedMood;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.repository.ListingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MoodDiscoveryService {
    
    AIService aiService;
    ListingRepository listingRepository;
    ObjectMapper objectMapper;
    
    // Curated mood definitions with emojis and characteristics
    private static final Map<String, MoodDefinition> MOODS = Map.ofEntries(
        Map.entry("adventurous", new MoodDefinition("🏔️", "Ready for thrills", "action-packed, fast-paced, exotic locales", List.of("exciting", "escapist", "curious"))),
        Map.entry("cozy", new MoodDefinition("☕", "Warm and comforting", "heartwarming, gentle pace, happy endings", List.of("relaxed", "nostalgic", "romantic"))),
        Map.entry("intellectual", new MoodDefinition("🧠", "Mind-expanding", "thought-provoking, complex ideas, challenging", List.of("curious", "philosophical", "analytical"))),
        Map.entry("escapist", new MoodDefinition("✨", "Another world", "fantasy, immersive worlds, far from reality", List.of("adventurous", "dreamy", "imaginative"))),
        Map.entry("emotional", new MoodDefinition("💔", "Feel deeply", "moving, character-driven, cathartic", List.of("introspective", "romantic", "melancholic"))),
        Map.entry("suspenseful", new MoodDefinition("🔍", "Edge of seat", "mysteries, twists, can't-put-down", List.of("thrilling", "curious", "intense"))),
        Map.entry("inspiring", new MoodDefinition("🌟", "Motivated", "uplifting, transformative, empowering", List.of("hopeful", "ambitious", "growth-minded"))),
        Map.entry("nostalgic", new MoodDefinition("📻", "Time travel", "classics, coming-of-age, simpler times", List.of("cozy", "reflective", "sentimental"))),
        Map.entry("romantic", new MoodDefinition("💕", "Love in the air", "relationships, chemistry, swoon-worthy", List.of("emotional", "hopeful", "passionate"))),
        Map.entry("dark", new MoodDefinition("🌙", "Embrace shadows", "gothic, horror, psychological", List.of("suspenseful", "intense", "atmospheric"))),
        Map.entry("philosophical", new MoodDefinition("🤔", "Deep thoughts", "existential, meaning of life, ethics", List.of("intellectual", "introspective", "challenging"))),
        Map.entry("playful", new MoodDefinition("🎭", "Light-hearted", "witty, humorous, entertaining", List.of("fun", "relaxed", "clever"))),
        Map.entry("focused", new MoodDefinition("🎯", "Learn something", "educational, practical, skill-building", List.of("intellectual", "ambitious", "productive"))),
        Map.entry("relaxed", new MoodDefinition("🌊", "Wind down", "easy reads, beach books, low stakes", List.of("cozy", "escapist", "light"))),
        Map.entry("intense", new MoodDefinition("🔥", "All-in experience", "gripping, complex, demanding", List.of("suspenseful", "emotional", "immersive")))
    );

    public MoodDiscoveryResponse discoverByMood(MoodDiscoveryRequest request) {
        String mood = request.getMood().toLowerCase().trim();
        MoodDefinition moodDef = MOODS.getOrDefault(mood, 
            new MoodDefinition("📚", "Custom mood", mood, List.of()));
        
        // Get available listings (active ones)
        List<Listing> availableListings = listingRepository.findAllWithDetails(
            ListingStatus.ACTIVE,
            PageRequest.of(0, 100)
        ).getContent();
        
        if (availableListings.isEmpty()) {
            return buildEmptyResponse(mood, moodDef);
        }
        
        // Try AI-powered recommendations first
        try {
            return getAIRecommendations(request, mood, moodDef, availableListings);
        } catch (Exception e) {
            log.warn("AI mood discovery failed, using fallback: {}", e.getMessage());
            return getFallbackRecommendations(request, mood, moodDef, availableListings);
        }
    }
    
    private MoodDiscoveryResponse getAIRecommendations(
            MoodDiscoveryRequest request,
            String mood,
            MoodDefinition moodDef,
            List<Listing> listings
    ) {
        // Build context about available books
        String booksContext = listings.stream()
            .limit(50) // Don't overwhelm the prompt
            .map(l -> String.format(
                "ID:%d | %s by %s | %s | Categories: %s",
                l.getId(),
                l.getBookMeta().getTitle(),
                l.getBookMeta().getAuthors().stream()
                    .map(a -> a.getName())
                    .collect(Collectors.joining(", ")),
                l.getBookMeta().getDescription() != null 
                    ? l.getBookMeta().getDescription().substring(0, Math.min(100, l.getBookMeta().getDescription().length())) 
                    : "No description",
                l.getBookMeta().getCategories().stream()
                    .map(c -> c.getName())
                    .collect(Collectors.joining(", "))
            ))
            .collect(Collectors.joining("\n"));
        
        String prompt = String.format("""
            You are a literary mood curator. A reader is in a "%s" mood (%s).
            
            Available books:
            %s
            
            Select the %d best matches for this mood. Return JSON only:
            {
              "moodDescription": "one-line poetic description of this reading mood",
              "atmosphereSuggestion": "brief sensory suggestion (lighting, music, drink)",
              "recommendations": [
                {
                  "listingId": <number>,
                  "matchScore": <0-100>,
                  "whyThisFits": "brief personal explanation",
                  "moodTags": ["tag1", "tag2"]
                }
              ]
            }
            
            Be specific, personal, and evocative. No generic responses.
            """,
            mood,
            moodDef.description,
            booksContext,
            request.getLimit() != null ? request.getLimit() : 10
        );
        
        String response = aiService.generateRecommendation(prompt);
        
        // Parse AI response
        return parseAIResponse(response, mood, moodDef, listings);
    }
    
    private MoodDiscoveryResponse parseAIResponse(
            String aiResponse,
            String mood,
            MoodDefinition moodDef,
            List<Listing> listings
    ) {
        try {
            // Extract JSON from response
            String json = extractJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<>() {});
            
            String moodDescription = (String) parsed.getOrDefault("moodDescription", moodDef.description);
            String atmosphereSuggestion = (String) parsed.getOrDefault("atmosphereSuggestion", 
                "Find a quiet corner with your favorite beverage.");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recs = (List<Map<String, Object>>) parsed.get("recommendations");
            
            // Map listing IDs to actual listings
            Map<Long, Listing> listingMap = listings.stream()
                .collect(Collectors.toMap(Listing::getId, l -> l));
            
            List<MoodRecommendation> recommendations = new ArrayList<>();
            if (recs != null) {
                for (Map<String, Object> rec : recs) {
                    Long listingId = ((Number) rec.get("listingId")).longValue();
                    Listing listing = listingMap.get(listingId);
                    if (listing != null) {
                        recommendations.add(MoodRecommendation.builder()
                            .listingId(listingId)
                            .title(listing.getBookMeta().getTitle())
                            .author(listing.getBookMeta().getAuthors().stream()
                                .map(a -> a.getName())
                                .collect(Collectors.joining(", ")))
                            .coverUrl(listing.getBookMeta().getCoverImageUrl())
                            .price(listing.getPrice().doubleValue())
                            .condition(listing.getCondition().name())
                            .matchScore(((Number) rec.getOrDefault("matchScore", 80)).doubleValue())
                            .whyThisFits((String) rec.getOrDefault("whyThisFits", "Great match for your mood"))
                            .moodTags(rec.get("moodTags") != null ? 
                                ((List<?>) rec.get("moodTags")).stream()
                                    .map(Object::toString)
                                    .collect(Collectors.toList()) 
                                : List.of())
                            .build());
                    }
                }
            }
            
            return MoodDiscoveryResponse.builder()
                .mood(mood)
                .moodDescription(moodDescription)
                .atmosphereSuggestion(atmosphereSuggestion)
                .recommendations(recommendations)
                .relatedMoods(getRelatedMoods(moodDef))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new RuntimeException("AI response parsing failed", e);
        }
    }
    
    private MoodDiscoveryResponse getFallbackRecommendations(
            MoodDiscoveryRequest request,
            String mood,
            MoodDefinition moodDef,
            List<Listing> listings
    ) {
        // Simple keyword-based matching as fallback
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        
        List<MoodRecommendation> recommendations = listings.stream()
            .limit(limit)
            .map(listing -> MoodRecommendation.builder()
                .listingId(listing.getId())
                .title(listing.getBookMeta().getTitle())
                .author(listing.getBookMeta().getAuthors().stream()
                    .map(a -> a.getName())
                    .collect(Collectors.joining(", ")))
                .coverUrl(listing.getBookMeta().getCoverImageUrl())
                .price(listing.getPrice().doubleValue())
                .condition(listing.getCondition().name())
                .matchScore(70.0 + new Random().nextDouble() * 20)
                .whyThisFits("Recommended for your " + mood + " mood")
                .moodTags(List.of(mood))
                .build())
            .collect(Collectors.toList());
        
        return MoodDiscoveryResponse.builder()
            .mood(mood)
            .moodDescription(moodDef.description)
            .atmosphereSuggestion("Find your perfect reading spot and enjoy.")
            .recommendations(recommendations)
            .relatedMoods(getRelatedMoods(moodDef))
            .build();
    }
    
    private MoodDiscoveryResponse buildEmptyResponse(String mood, MoodDefinition moodDef) {
        return MoodDiscoveryResponse.builder()
            .mood(mood)
            .moodDescription(moodDef.description)
            .atmosphereSuggestion("Check back soon for books matching your mood!")
            .recommendations(List.of())
            .relatedMoods(getRelatedMoods(moodDef))
            .build();
    }
    
    private List<RelatedMood> getRelatedMoods(MoodDefinition moodDef) {
        return moodDef.relatedMoods.stream()
            .filter(MOODS::containsKey)
            .limit(3)
            .map(related -> {
                MoodDefinition def = MOODS.get(related);
                return RelatedMood.builder()
                    .name(related)
                    .emoji(def.emoji)
                    .description(def.tagline)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new IllegalArgumentException("No JSON found in response");
    }
    
    /**
     * Get all available moods with their definitions
     */
    public List<Map<String, Object>> getAvailableMoods() {
        return MOODS.entrySet().stream()
            .map(e -> Map.<String, Object>of(
                "name", e.getKey(),
                "emoji", e.getValue().emoji,
                "tagline", e.getValue().tagline,
                "description", e.getValue().description
            ))
            .collect(Collectors.toList());
    }
    
    // Internal record for mood definitions
    private record MoodDefinition(String emoji, String tagline, String description, List<String> relatedMoods) {}
}
