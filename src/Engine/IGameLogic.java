package Engine;

public interface IGameLogic {

    void init(WindowMangaer window) throws Exception;

    void input(WindowMangaer window,MouseInput mouseInput);

    void update(float interval,MouseInput mouseInput);

    void render(WindowMangaer window);

    void cleanup();

}
