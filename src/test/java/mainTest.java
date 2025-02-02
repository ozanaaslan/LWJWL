import com.github.ozanaaslan.lwjwl.LWJWL;
import com.github.ozanaaslan.lwjwl.util.JsonParser;
import com.github.ozanaaslan.lwjwl.web.endpoint.EndpointController;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Endpoint;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Param;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.GET;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.POST;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.Response;
import lombok.SneakyThrows;

public class mainTest {

    @SneakyThrows
    public static void main(String[] args) {
        LWJWL obj = new LWJWL(8080);
        obj.register(mainTest.class);

    }

    @Endpoint("/helloworld") @GET
    public static Response someEndpoint(EndpointController endpointController){
        return Response.json(200, "You got it!");
    }

    @Endpoint("/helloworld") @POST
    public static Response someEndpoint(EndpointController endpointController, @Param("id") String id){
        return Response.json(200, "You will only see this when you post");
    }

    @Endpoint("/person")
    public static Person getPerson(EndpointController endpointController){
        Person p = new Person("John Doe", 31);
        System.out.println(((Person) JsonParser.toObject(JsonParser.toJson(p))).getName());
        return p;
    }



}
