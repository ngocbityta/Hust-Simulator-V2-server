package com.hustsimulator.streaming.livekit;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiveKitServiceImpl implements LiveKitService {

    @Value("${livekit.api-key}")
    private String apiKey;

    @Value("${livekit.api-secret}")
    private String apiSecret;

    /**
     * Create a LiveKit access token for a participant.
     *
     * @param roomName    the LiveKit room name
     * @param identity    unique identity for this participant (userId)
     * @param displayName human-readable display name
     * @param canPublish  true = publisher (streamer), false = subscriber (viewer)
     * @return signed JWT string
     */
    public String createToken(String roomName, String identity, String displayName, boolean canPublish) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setIdentity(identity);
        token.setName(displayName);

        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName),
                new CanPublish(canPublish),
                new CanSubscribe(true)
        );

        return token.toJwt();
    }
}
