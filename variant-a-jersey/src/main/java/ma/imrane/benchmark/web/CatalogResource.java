package ma.imrane.benchmark.web;
import ma.imrane.benchmark.service.CatalogService;
import ma.imrane.benchmark.service.CatalogService.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
@Component @Path("/") @Produces(MediaType.APPLICATION_JSON)
public class CatalogResource {
    private final CatalogService service;
    public CatalogResource(CatalogService service) {this.service=service;}
    @GET @Path("categories") public Page<CategoryView> categories(@QueryParam("page") @DefaultValue("0") int page,@QueryParam("size") @DefaultValue("50") int size) {return service.categories(page,size);}
    @GET @Path("categories/{id}") public CategoryView category(@PathParam("id") Long id) {return service.category(id);}
    @POST @Consumes(MediaType.APPLICATION_JSON) @Path("categories") public Response addCategory(CategoryInput input) {return Response.status(201).entity(service.saveCategory(null,input)).build();}
    @PUT @Consumes(MediaType.APPLICATION_JSON) @Path("categories/{id}") public CategoryView updateCategory(@PathParam("id") Long id,CategoryInput input) {return service.saveCategory(id,input);}
    @DELETE @Path("categories/{id}") public Response deleteCategory(@PathParam("id") Long id) {service.deleteCategory(id);return Response.noContent().build();}
    @GET @Path("items") public Page<ItemView> items(@QueryParam("categoryId") Long categoryId,@QueryParam("page") @DefaultValue("0") int page,@QueryParam("size") @DefaultValue("50") int size) {return service.items(categoryId,page,size);}
    @GET @Path("categories/{id}/items") public Page<ItemView> related(@PathParam("id") Long id,@QueryParam("page") @DefaultValue("0") int page,@QueryParam("size") @DefaultValue("50") int size) {service.category(id);return service.items(id,page,size);}
    @GET @Path("items/{id}") public ItemView item(@PathParam("id") Long id) {return service.item(id);}
    @POST @Consumes(MediaType.APPLICATION_JSON) @Path("items") public Response addItem(ItemInput input) {return Response.status(201).entity(service.saveItem(null,input)).build();}
    @PUT @Consumes(MediaType.APPLICATION_JSON) @Path("items/{id}") public ItemView updateItem(@PathParam("id") Long id,ItemInput input) {return service.saveItem(id,input);}
    @DELETE @Path("items/{id}") public Response deleteItem(@PathParam("id") Long id) {service.deleteItem(id);return Response.noContent().build();}
}
