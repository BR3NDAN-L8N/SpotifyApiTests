package utils.restResources;
// JAVA
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
// REST-ASSURED
import io.restassured.response.Response;
// JSON
import org.json.JSONArray;
import org.json.JSONObject;
// MINE
import models.Playlist;
import utils.Endpoints;
import utils.JSON;


/**
 * Logic for dealing with Spotify's endpoints related to Playlists
 */
public class RestfulPlaylist {

    //*************************
    //*************************
    //          GET
    //*************************
    //*************************

    /**
     * GET all playlists in the "featured" section from the browse page.
     *
     * @return example...
     * <br/>{
     * <br/>_  "albums": {
     * <br/>_ _    "href": "https://api.spotify.com/v1/me/shows?offset=0&limit=20\n",
     * <br/>_ _    "items": [
     * <br/>_ _ _      {}
     * <br/>_ _    ],
     * <br/>_ _    "limit": 20,
     * <br/>_ _    "next": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
     * <br/>_ _    "offset": 0,
     * <br/>_ _    "previous": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
     * <br/>_ _    "total": 4
     * <br/>_  },
     * <br/>  "message": "string"
     * <br/>}
     */
    public static List<Playlist> getAllPlaylists_featured() {
        String res = RestResource.get("browse/featured-playlists").asString();
        JSONArray playlists_json = new JSONObject(res).getJSONObject("playlists").getJSONArray("items");

        return JSON.parse_JSONArray_to_ListPlaylists(playlists_json);
    }

    /**
     * Get all Playlists for 1 user.
     * @param userId ID of the user
     * @return List of Playlists, included values are; ID, NAME, DESCRIPTION, URI
     */
    public static List<Playlist> getAllPlaylists_forSingleUser(String userId) {
        // get playlists
        String response = RestResource.get(Endpoints.USERS + userId + '/' + Endpoints.PLAYLISTS).asString();

        // parse response
        JSONObject json = new JSONObject(response);
        JSONArray playlists = json.getJSONArray("items");

        // create return object
        List<Playlist> returnObject = new ArrayList<>();

        // if no playlists return returnObject
        if (playlists.isEmpty()) return returnObject;

        // loop playlists, create new Playlist obj for each and assign to returnObject
        for (Object playlist : playlists) {
            // cast playlist to use JSONObject methods
            JSONObject jsObject = (JSONObject) playlist;
            // create Playlist object
            Playlist newPlaylist = new Playlist();
            newPlaylist.setId(String.valueOf(jsObject.getString("id")));
            newPlaylist.setName(jsObject.get("name").toString());
            newPlaylist.setDescription(jsObject.get("description").toString());
            newPlaylist.setUri(jsObject.get("uri").toString());
            // add to return object
            returnObject.add(newPlaylist);
        }

        return returnObject;
    }

    /**
     * GET a playlist by a playlists ID
     *
     * @param playlistId ID of the playlist you want
     * @return Playlist object as a Response object
     */
    public static Playlist getPlaylist_byId(String playlistId) {
        return RestResource.get(Endpoints.PLAYLISTS + '/' + playlistId).as(Playlist.class);
    }

    /**
     * Get all items in a Playlist object, the items are called "tracks"
     * @param playlistId ID of the playlist in question, try <strong>1QOHh3S7UQXDrdr7cSnRR7</strong>
     * @return A Tracks object. Tracks.getItems() will get the array of tracks as List>Object<. An example of on item: models/item_in_a_Tracks_object.json
     */
    public static List<JSONObject> getPlaylistsTracks(String playlistId) {
        // GET TRACKS
        String res = RestResource.get(Endpoints.PLAYLISTS + '/' + playlistId + '/' + Endpoints.TRACKS).asString();

        // CONVERT TO SOMETHING USEFUL
        // get the actual TRACKS object array
        JSONObject json = new JSONObject(res);
        JSONArray items = json.getJSONArray("items");
        // useful return object
        List<JSONObject> tracks = new ArrayList<>();
        if (items.length() < 1) return tracks;
        // iterate TRACKS array and move to useful return object
        for (Object item : items) {
            JSONObject jsItem = (JSONObject) item;
            if (JSONObject.NULL.equals(jsItem.get("track"))) continue;
            JSONObject track = (JSONObject) jsItem.get("track");
            tracks.add(track);
        }
        // return the useful list of objects
        return tracks;
    }

    //*************************
    //*************************
    //         DELETE
    //*************************
    //*************************

    /**
     * Simulates deleting a playlist. There is no endpoint to delete a playlist. When a user logs into Spotify and manually deletes a playlist it's not actually deleted but rather the playlist is unfollowed.
     *
     * @param playlistId id of the playlist to be deleted/unfollowed
     * @return nothing is returned from the endpoint on a successful DELETE. Unsuccessful request return
     * <br/>{
     * <br/>_   "error": {
     * <br/>_ _   "status": 400,
     * <br/>_ _   "message": "string"
     * <br/>  } }
     */
    public static void deletePlaylist_byId(String playlistId) {
        RestResource.delete(Endpoints.PLAYLISTS + '/' + playlistId + "/followers");
    }

    public static void deleteAllPlaylists_forSingleUser(String userId) {
        List<Playlist> playlists = RestfulPlaylist.getAllPlaylists_forSingleUser(userId);

        for (Playlist playlist : playlists) {
            RestfulPlaylist.deletePlaylist_byId(playlist.getId());
        }
    }

    //*************************
    //*************************
    //          POST
    //*************************
    //*************************

    /**
     * Create a playlist for the specified user
     *
     * @param userId   ID of the user who wants a new playlist
     * @param playlist The Playlist object to be created
     * @return returns the newly created playlist as a Playlist object
     */
    public static Playlist createPlaylist(String userId, Playlist playlist) {
        return RestResource.post(Endpoints.USERS + userId + '/' + Endpoints.PLAYLISTS, playlist).as(Playlist.class);
    }

    /**
     * Add Tracks/items to an already existing Playlist in Spotify's DB
     * @param playlistId ID of the Playlist to add Tracks to
     * @param items A List>String<, Strings are URIs for tracks or episodes on Spotify
     * @return
     */
    public static void addItemsToPlaylist(String playlistId, List<String> items) {
        RestResource.post(Endpoints.PLAYLISTS + '/' + playlistId + '/' + Endpoints.TRACKS, items);
    }

    //*************************
    //*************************
    //          PUT
    //*************************
    //*************************

    /**
     * Update the details of a Playlist in the DB
     * @param playlistId ID of the playlist to update
     * @param updatedPlaylist A Playlist object reflecting the desired changes.
     * @return Example:
     * <br/>{
     * <br/>_ "error": {
     * <br/>_ _ "status": 400,
     * <br/>_ _ "message": "string"
     * <br/>_ }
     * <br/>}
     */
    public static JSONObject updatePlaylistDetails(String playlistId, Playlist updatedPlaylist) {
        Response res = RestResource.put(Endpoints.PLAYLISTS + '/' + playlistId, updatedPlaylist);

        if (res.getStatusCode() != 200) {
            // parse response
            JSONObject json = new JSONObject(res);
            return json;
        } else {
            return new JSONObject(
                "{\n" +
                        "  \"error\": {\n" +
                        "   \"status\": 200,\n" +
                        "   \"message\": \"success\"\n" +
                        "  }\n" +
                        "}"
            );
        }
    }
}
