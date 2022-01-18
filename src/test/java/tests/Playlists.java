package tests;
// JAVA
import java.util.List;
// TEST-NG
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
// REST-ASSURED
import io.restassured.response.Response;
// MINE
import utils.Endpoints;
import utils.restResources.RestResource;
import utils.restResources.RestfulPlaylist;
import models.Playlist;


/**
 * Tests pertaining to the "playlist" endpoint for Spotify's API
 */
public class Playlists {

    private String userId;

    @BeforeTest
    public void setup() {
        // GET USER's ID
        // send request
        Response userData = RestResource.get(Endpoints.ME);
        // extracting users' ID
        userId = userData.path("id");
    }

    //*************************
    //*************************
    //      POST TESTS
    //*************************
    //*************************

    @Test
    public void createPlaylist() {

        // CREATE THE PLAYLIST
        // create request body
        Playlist playlist = new Playlist();
        playlist.setName("New Playlist " + System.currentTimeMillis());
        playlist.setDescription("Auto generated");
        playlist.setPublic(false);
        // send request
        Playlist res = RestfulPlaylist.createPlaylist(userId, playlist);

        // ASSERT
        Assert.assertEquals(res.getName(), playlist.getName());
    }

    //*************************
    //*************************
    //       GET TESTS
    //*************************
    //*************************

    /**
     * GET all playlists belonging to a user
     */
    @Test
    public void getUsersPlaylists() {
        RestfulPlaylist.getAllPlaylists_forSingleUser(this.userId);
    }

    /**
     * GET one playlist by its ID
     */
    @Test
    public void getPlaylistByPlaylistId() {
        // CREATE PLAYLIST
        // create request body
        Playlist playlist = new Playlist();
        playlist.setName("Get me by my ID! " + System.currentTimeMillis());
        playlist.setDescription("Made to be gotten");
        playlist.setPublic(false);
        // send request
        Playlist res = RestfulPlaylist.createPlaylist(userId, playlist);


        // GET PLAYLIST BY ID
        Playlist playlist_gotById = RestfulPlaylist.getPlaylist_byId(res.getId());

        // ASSERT
        Assert.assertEquals(playlist.getName(), playlist_gotById.getName());
        Assert.assertEquals(playlist.getDescription(), playlist_gotById.getDescription());
        Assert.assertEquals(playlist.getPublic(), playlist_gotById.getPublic());
    }

    /**
     * GET the items from a playlist.
     *
     * "items" could be songs, podcasts, etc.
     */
    @Test
    public void getPlaylistItems() {
        List<Playlist> playlists = RestfulPlaylist.getAllPlaylists_forSingleUser(this.userId);

        String playlistId;
        if (playlists.size() == 0) {
            // CREATE PLAYLIST
            // create request body
            Playlist playlist = new Playlist();
            playlist.setName("Get me by my ID! " + System.currentTimeMillis());
            playlist.setDescription("Made to be gotten");
            playlist.setPublic(false);
            // send request
            Playlist res = RestfulPlaylist.createPlaylist(userId, playlist);
            playlistId = res.getId();
        } else {
            playlistId = playlists.get(0).getId();
        }
        RestfulPlaylist.getPlaylistsTracks(playlistId);
    }

    //*************************
    //*************************
    //      DELETE TESTS
    //*************************
    //*************************

    /**
     * DELETE a playlist by its ID.
     *
     * Spotify doesn't actually delete playlists just makes the user unfollow it. So, by "delete" we always mean unfollow. It will no longer show up the in the users' list of playlists
     */
    @Test
    public void deletePlaylistById() {

        // GET PLAYLISTS
        List<Playlist> res_usersPlaylists_beforeDel = RestfulPlaylist.getAllPlaylists_forSingleUser(this.userId);

        // if there are no playlists, CREATE a PLAYLIST
        if (res_usersPlaylists_beforeDel.size() == 0) {
            // create request body
            Playlist playlist = new Playlist();
            playlist.setName("New Playlist " + System.currentTimeMillis());
            playlist.setDescription("Auto generated");
            playlist.setPublic(false);
            // send request
            RestfulPlaylist.createPlaylist(userId, playlist);
            // get playlists
            res_usersPlaylists_beforeDel = RestfulPlaylist.getAllPlaylists_forSingleUser(this.userId);
        }

        // GET ID OF A PLAYLIST
        String playlistId = res_usersPlaylists_beforeDel.get(0).getId();

        // DELETE PLAYLIST
        RestfulPlaylist.deletePlaylist_byId(playlistId);

        // GET PLAYLISTS
        List<Playlist> res_usersPlaylists_afterDel = RestfulPlaylist.getAllPlaylists_forSingleUser(this.userId);

        // ASSERT
        // expect 1 less playlist after delete
        Assert.assertEquals(res_usersPlaylists_beforeDel.size() - 1, res_usersPlaylists_afterDel.size());
        // expect ID of deleted playlist to not be in list
        if (res_usersPlaylists_afterDel.size() != 0) {
            boolean isPlaylistDeleted = true;
            for (Playlist playlist : res_usersPlaylists_afterDel) {
                if (playlist.getId().equals(playlistId)) {
                    isPlaylistDeleted = false;
                    break;
                }
            }
            Assert.assertTrue(isPlaylistDeleted);
        }

    }
}
