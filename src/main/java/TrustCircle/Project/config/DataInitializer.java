package TrustCircle.Project.config;

import TrustCircle.Project.model.*;
import TrustCircle.Project.repository.*;
import TrustCircle.Project.service.DynamicTrustService;
import TrustCircle.Project.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityParticipantRepository participantRepository;

    @Autowired
    private PeerReviewRepository peerReviewRepository;

    @Autowired
    private SafetyAlertRepository safetyAlertRepository;

    @Autowired
    private DynamicTrustService dynamicTrustService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Seed Verified Community Members
        User aarav = new User();
        aarav.setEmail("aarav@trustcircle.io");
        aarav.setPassword(PasswordUtil.hashPassword("AaravSecure123!"));
        aarav.setFullName("Aarav Sharma");
        aarav.setPhone("+1 (555) 234-5678");
        aarav.setBio("Passionate badminton player, runner, and weekend tech explorer. Love meeting genuine people for outdoor activities.");
        aarav.setClaimedCity("San Francisco");
        aarav.setVerifiedLatitude(37.7749);
        aarav.setVerifiedLongitude(-122.4194);
        aarav.setLocationAccuracyMeters(12.0);
        aarav.setLocationVerified(true);
        aarav.setLocationVerificationNotes("GPS physically confirmed via browser sensor (accuracy: ~12m)");
        aarav.setIdentityVerified(true);
        aarav.setVerificationStatus("VERIFIED");
        aarav.setInterests("Badminton,Running,Tech,Travel");
        aarav.setSocialVibe("Active & Outdoorsy");
        aarav.setBudgetPreference("MODERATE");
        aarav.setTypicalAvailability("WEEKENDS");
        aarav.setSafetyPactAgreed("YES");
        aarav.setActivitiesAttended(6);
        aarav.setActivitiesHosted(3);
        aarav = userRepository.save(aarav);

        User sarah = new User();
        sarah.setEmail("sarah@trustcircle.io");
        sarah.setPassword(PasswordUtil.hashPassword("SarahSecure123!"));
        sarah.setFullName("Sarah Jenkins");
        sarah.setPhone("+1 (555) 345-6789");
        sarah.setBio("Computer science graduate student researching distributed systems. Looking for focused coffee shop study sessions.");
        sarah.setClaimedCity("San Francisco");
        sarah.setVerifiedLatitude(37.7833);
        sarah.setVerifiedLongitude(-122.4167);
        sarah.setLocationAccuracyMeters(8.0);
        sarah.setLocationVerified(true);
        sarah.setLocationVerificationNotes("GPS physically confirmed via browser sensor (accuracy: ~8m)");
        sarah.setIdentityVerified(true);
        sarah.setVerificationStatus("VERIFIED");
        sarah.setInterests("Study,Coding,Coffee,Badminton,Books");
        sarah.setSocialVibe("Focused & Laser-Sharp");
        sarah.setBudgetPreference("BUDGET_FRIENDLY");
        sarah.setTypicalAvailability("WEEKENDS");
        sarah.setSafetyPactAgreed("YES");
        sarah.setActivitiesAttended(4);
        sarah.setActivitiesHosted(2);
        sarah = userRepository.save(sarah);

        User carlos = new User();
        carlos.setEmail("carlos@trustcircle.io");
        carlos.setPassword(PasswordUtil.hashPassword("CarlosSecure123!"));
        carlos.setFullName("Carlos Mendez");
        carlos.setPhone("+1 (555) 456-7890");
        carlos.setBio("Trail runner and amateur landscape photographer. Let's explore local hiking trails with verified, safety-conscious companions.");
        carlos.setClaimedCity("San Francisco");
        carlos.setVerifiedLatitude(37.7600);
        carlos.setVerifiedLongitude(-122.4400);
        carlos.setLocationAccuracyMeters(15.0);
        carlos.setLocationVerified(true);
        carlos.setLocationVerificationNotes("GPS physically confirmed via browser sensor (accuracy: ~15m)");
        carlos.setIdentityVerified(true);
        carlos.setVerificationStatus("VERIFIED");
        carlos.setInterests("Hiking,Travel,Photography,Camping");
        carlos.setSocialVibe("Active & Outdoorsy");
        carlos.setBudgetPreference("FLEXIBLE");
        carlos.setTypicalAvailability("WEEKENDS");
        carlos.setSafetyPactAgreed("YES");
        carlos.setActivitiesAttended(5);
        carlos.setActivitiesHosted(4);
        carlos = userRepository.save(carlos);

        User maya = new User();
        maya.setEmail("maya@trustcircle.io");
        maya.setPassword(PasswordUtil.hashPassword("MayaSecure123!"));
        maya.setFullName("Maya Patel");
        maya.setPhone("+1 (555) 567-8901");
        maya.setBio("Designer & board games aficionado. Always up for Catan, chess, or discovering local acoustic music gigs.");
        maya.setClaimedCity("San Francisco");
        maya.setVerifiedLatitude(37.7690);
        maya.setVerifiedLongitude(-122.4467);
        maya.setLocationAccuracyMeters(10.0);
        maya.setLocationVerified(true);
        maya.setLocationVerificationNotes("GPS physically confirmed via browser sensor (accuracy: ~10m)");
        maya.setIdentityVerified(true);
        maya.setVerificationStatus("VERIFIED");
        maya.setInterests("Board Games,Social,Design,Events,Coffee");
        maya.setSocialVibe("Chill & Casual");
        maya.setBudgetPreference("MODERATE");
        maya.setTypicalAvailability("WEEKDAY_EVENINGS");
        maya.setSafetyPactAgreed("YES");
        maya.setActivitiesAttended(7);
        maya.setActivitiesHosted(2);
        maya = userRepository.save(maya);

        // 2. Seed Activities
        Activity badminton = new Activity();
        badminton.setTitle("Weekend Badminton Doubles & Fitness Meet");
        badminton.setDescription("Looking for 3 fellow verified players for friendly intermediate doubles at the indoor court. Equipment provided, bring clean court shoes!");
        badminton.setCategory("SPORTS");
        badminton.setVenue("Bay Club Sports Center, Court 3");
        badminton.setCity("San Francisco");
        badminton.setLatitude(37.7780);
        badminton.setLongitude(-122.4050);
        badminton.setActivityDate(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
        badminton.setBudget("LOW");
        badminton.setMinTrustScore(60);
        badminton.setMaxParticipants(4);
        badminton.setStatus("OPEN");
        badminton.setCreator(aarav);
        badminton = activityRepository.save(badminton);
        participantRepository.save(new ActivityParticipant(badminton, aarav, "HOST"));

        Activity studyJam = new Activity();
        studyJam.setTitle("Deep Work & AI Coding Study Session");
        studyJam.setDescription("Quiet Pomodoro study session at Blue Bottle Coffee. Perfect for programmers, researchers, or anyone tackling deep projects with zero distractions.");
        studyJam.setCategory("STUDY");
        studyJam.setVenue("Blue Bottle Coffee, Mint Plaza");
        studyJam.setCity("San Francisco");
        studyJam.setLatitude(37.7815);
        studyJam.setLongitude(-122.4060);
        studyJam.setActivityDate(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        studyJam.setBudget("FREE");
        studyJam.setMinTrustScore(50);
        studyJam.setMaxParticipants(5);
        studyJam.setStatus("OPEN");
        studyJam.setCreator(sarah);
        studyJam = activityRepository.save(studyJam);
        participantRepository.save(new ActivityParticipant(studyJam, sarah, "HOST"));

        Activity hike = new Activity();
        hike.setTitle("Twin Peaks Sunset Scenic Hike & Photography");
        hike.setDescription("A relaxed 6km loop trail up Twin Peaks catching the golden hour skyline. Verified members only. Bring water and windbreaker.");
        hike.setCategory("TRAVEL");
        hike.setVenue("Twin Peaks Trailhead South");
        hike.setCity("San Francisco");
        hike.setLatitude(37.7544);
        hike.setLongitude(-122.4477);
        hike.setActivityDate(LocalDateTime.now().plusDays(3).withHour(17).withMinute(30));
        hike.setBudget("FREE");
        hike.setMinTrustScore(70);
        hike.setMaxParticipants(6);
        hike.setStatus("OPEN");
        hike.setCreator(carlos);
        hike = activityRepository.save(hike);
        participantRepository.save(new ActivityParticipant(hike, carlos, "HOST"));

        Activity boardGames = new Activity();
        boardGames.setTitle("Catan & Strategy Board Games Social");
        boardGames.setDescription("Casual Saturday afternoon board games at Gamescape cafe. Beginner friendly, great vibes and friendly people!");
        boardGames.setCategory("SOCIAL");
        boardGames.setVenue("Gamescape Lounge, Divisadero St");
        boardGames.setCity("San Francisco");
        boardGames.setLatitude(37.7730);
        boardGames.setLongitude(-122.4370);
        boardGames.setActivityDate(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0));
        boardGames.setBudget("LOW");
        boardGames.setMinTrustScore(55);
        boardGames.setMaxParticipants(6);
        boardGames.setStatus("OPEN");
        boardGames.setCreator(maya);
        boardGames = activityRepository.save(boardGames);
        participantRepository.save(new ActivityParticipant(boardGames, maya, "HOST"));

        // 3. Seed Peer Reviews
        PeerReview r1 = new PeerReview();
        r1.setActivity(badminton);
        r1.setReviewer(sarah);
        r1.setReviewee(aarav);
        r1.setRating(5.0);
        r1.setPunctualityScore(5);
        r1.setFriendlinessScore(5);
        r1.setSafetyScore(5);
        r1.setFeedback("Aarav was super organized and arrived 10 mins early with fresh shuttles. Fantastic, trustworthy host!");
        r1.setBadgeAwarded("Top Punctual");
        peerReviewRepository.save(r1);

        PeerReview r2 = new PeerReview();
        r2.setActivity(studyJam);
        r2.setReviewer(aarav);
        r2.setReviewee(sarah);
        r2.setRating(5.0);
        r2.setPunctualityScore(5);
        r2.setFriendlinessScore(5);
        r2.setSafetyScore(5);
        r2.setFeedback("Sarah was incredible! Great study focus and shared awesome insights on machine learning pipelines.");
        r2.setBadgeAwarded("Focused Mind");
        peerReviewRepository.save(r2);

        // 4. Seed Safety Alert Demonstration
        SafetyAlert alert = new SafetyAlert();
        alert.setUser(carlos);
        alert.setAlertType("GPS_SPOOFING");
        alert.setRiskLevel("LOW");
        alert.setDetails("Minor GPS jitter detected during initial browser sensor calibration. Automatically reconciled.");
        alert.setResolved(true);
        alert.setResolutionNotes("Verified genuine mobile browser telemetry.");
        safetyAlertRepository.save(alert);

        // Recalculate trust scores for seeded members
        dynamicTrustService.recalculateTrustScore(aarav);
        dynamicTrustService.recalculateTrustScore(sarah);
        dynamicTrustService.recalculateTrustScore(carlos);
        dynamicTrustService.recalculateTrustScore(maya);
    }
}
