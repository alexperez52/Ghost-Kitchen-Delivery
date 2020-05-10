package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class RestaurantController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    ReviewsRepository reviewsRepository;

    @RequestMapping(value ="/restaurantAddress", method = RequestMethod.POST)
    public ResponseEntity<Object> createOrEditAddress(@RequestBody Address address, @CurrentUser MyUserPrincipal principal) {
        if(address != null) {
            if (userRepository.getUserByUsername(principal.getUsername()).getRestaurant().getAddress() == null) {
                userRepository.getUserByUsername(principal.getUsername()).getRestaurant().setAddress(address);
                addressRepository.save(address);
            } else {
                Address updateAddress = userRepository.getUserByUsername(principal.getUsername()).getRestaurant().getAddress();

                UserController.updateAddress(address, updateAddress);
                addressRepository.save(updateAddress);
            }
        }

        return new ResponseEntity<>("ok", HttpStatus.OK);

    }


    @RequestMapping(value = "/owner", method = RequestMethod.GET)
    public boolean hasRestaurant(@CurrentUser MyUserPrincipal principal){
        User user = userRepository.findByUsername(principal.getUsername()).get();
        return user.getRestaurant() != null;
    }

    @RequestMapping(value = "/owner/restaurants", method = RequestMethod.POST)
    public ResponseEntity<Object> createRestaurant(@RequestBody Restaurant restaurant, @CurrentUser MyUserPrincipal principal) {

        if(userRepository.findById(principal.getId()).isPresent()){
            if(userRepository.findById(principal.getId()).get().getRestaurant() == null) {

                userRepository.findById(principal.getId()).get().setRestaurant(restaurant);
                restaurantRepository.save(restaurant);

            }
            else{
                Restaurant updateRest =  userRepository.findById(principal.getId()).get().getRestaurant();
                if(restaurant.getRestaurantName() != null) {
                    updateRest.setRestaurantName(restaurant.getRestaurantName());
                }
                if(restaurant.getCategory() != null) {
                    updateRest.setCategory(restaurant.getCategory());
                }
                if(restaurant.getRatings() != null) {
                    updateRest.setRatings(restaurant.getRatings());
                }
                if(restaurant.getImageURL() != null) {
                    updateRest.setImageURL(restaurant.getImageURL());
                }
                restaurantRepository.save(updateRest);
            }
        }

        return new ResponseEntity<>("Done", HttpStatus.ACCEPTED);
    }



    @RequestMapping(value = "owner/restaurants/add", method = RequestMethod.PUT)
    public ResponseEntity<Object> removeItem(@RequestBody MenuItem item, @CurrentUser MyUserPrincipal principal){
        MenuItem deletionItem = itemRepository.getItemById(item.getId());
        if(userRepository.findById(principal.getId()).isPresent()){
               if(itemRepository.findItemById(deletionItem.getId()).isPresent()){
                itemRepository.delete(deletionItem);
                }
        }


        return new ResponseEntity<>("Done", HttpStatus.OK);
    }


    @RequestMapping(value = "/owner/restaurants/add", method = RequestMethod.POST)
    public ResponseEntity<Object> addItem(@RequestBody MenuItem item, @CurrentUser MyUserPrincipal principal){

        if(userRepository.findById(principal.getId()).isPresent()){


            item.setRestaurant(userRepository.findById(principal.getId()).get().getRestaurant());
        }
        itemRepository.save(item);

        return new ResponseEntity<Object>("Done", HttpStatus.OK);
    }

    @RequestMapping(value="/restaurants", method = RequestMethod.GET)
    public ResponseEntity<Object> getRestaurants(){

        return new ResponseEntity<Object>(restaurantRepository.getAllByIdNotNull(), HttpStatus.OK);
    }

    @RequestMapping(value="/items", method = RequestMethod.GET)
    public ResponseEntity<Object> getItems(@CurrentUser MyUserPrincipal principal){
        User user = userRepository.findByUsername(principal.getUsername()).get();

        return new ResponseEntity<>(itemRepository.findByRestaurant(userRepository.findByUsername(user.getUsername()).get().getRestaurant()), HttpStatus.OK);
    }

    @RequestMapping(value="/items", method = RequestMethod.PUT)
    public ResponseEntity<Object> editItems(@RequestBody MenuItem item, @CurrentUser MyUserPrincipal principal){

        if(itemRepository.findItemById(item.getId()).isPresent()){
            itemRepository.getItemById(item.getId()).setName(item.getName());
            itemRepository.getItemById(item.getId()).setDescription(item.getDescription());
            itemRepository.getItemById(item.getId()).setPrice(item.getPrice());
            itemRepository.getItemById(item.getId()).setImageURL(item.getImageURL());

            itemRepository.save(itemRepository.getItemById(item.getId()));
        }


        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value ="/specificRestaurant", method= RequestMethod.POST)
    public ResponseEntity<Object> getRestaurant(@RequestBody Restaurant restaurant){

        Long id = restaurant.getId();


        return new ResponseEntity<>(restaurantRepository.getRestaurantById(id), HttpStatus.OK);
    }


    @RequestMapping(value="/restaurantItems", method = RequestMethod.POST)
    public ResponseEntity<Object> getRestaurantItems(@RequestBody Restaurant restaurant){
        Restaurant restaurantWithItems = restaurantRepository.getRestaurantById(restaurant.getId());
        return new ResponseEntity<>( itemRepository.findByRestaurant(restaurantWithItems), HttpStatus.OK);
    }


    @RequestMapping(value="/invoice", method = RequestMethod.POST)
    public ResponseEntity<Object> createInvoice(@RequestBody Invoice invoice, @CurrentUser MyUserPrincipal principal){
        User user = userRepository.getUserByUsername(principal.getUsername());
        Restaurant foundRestaurant = restaurantRepository.getRestaurantById(invoice.getRestaurant().getId());

        invoice.setUser(user);
        invoice.setRestaurant(foundRestaurant);
        invoiceRepository.save(invoice);

        return new ResponseEntity<>(invoice, HttpStatus.OK);

    }

    @RequestMapping(value="/userInvoices", method = RequestMethod.GET)
    public ResponseEntity<Object> userInvoices(@CurrentUser MyUserPrincipal principal){
        User user = userRepository.findByUsername(principal.getUsername()).get();

        return new ResponseEntity<>(invoiceRepository.findAllByUser(user), HttpStatus.OK);
    }


    @RequestMapping(value="/specificInvoices", method = RequestMethod.GET)
    public ResponseEntity<Object> getInvoices(@CurrentUser MyUserPrincipal principal){
        User user = userRepository.findByUsername(principal.getUsername()).get();


        return new ResponseEntity<>(invoiceRepository.findAllByRestaurant(user.getRestaurant()), HttpStatus.OK);
    }

    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public ResponseEntity<Object> getOrdersNotComplete(){

        return new ResponseEntity<>(invoiceRepository.findAllByIsCompleteIsFalse(), HttpStatus.OK);
    }

    


    @RequestMapping(value = "/acceptOrder", method = RequestMethod.PUT)
    public ResponseEntity<Object> acceptOrder(@RequestBody Invoice invoice){

       Invoice updateInvoice = invoiceRepository.getById(invoice.getId());
       System.out.println(invoice.getId());
        updateInvoice.setInProgress(true);
        invoiceRepository.save(updateInvoice);

        return new ResponseEntity<>("Done", HttpStatus.OK);
    }
    @RequestMapping(value = "/completeOrder", method = RequestMethod.PUT)
    public ResponseEntity<Object> completeOrder(@RequestBody Invoice invoice){

        Invoice updateInvoice = invoiceRepository.getById(invoice.getId());
        System.out.println(invoice.getId());
        updateInvoice.setComplete(true);
        invoiceRepository.save(updateInvoice);

        return new ResponseEntity<>("Done", HttpStatus.OK);
    }

    @RequestMapping(value = "/rate", method = RequestMethod.POST)
    public ResponseEntity<Object> rate(@RequestBody Restaurant restaurant, @CurrentUser MyUserPrincipal principal){

        Restaurant updateRestaurant = restaurantRepository.getRestaurantById(restaurant.getId());
        User user = userRepository.getUserByUsername(principal.getUsername());
        Reviews reviews = new Reviews();
        reviews.setRestaurant(updateRestaurant);
        reviews.setUser(user);
        reviews.setRatings(restaurant.getRatings());
        reviews.setMessage(restaurant.getMessage());
        reviewsRepository.save(reviews);

        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @RequestMapping(value = "/restaurantRatings", method = RequestMethod.POST)
    public ResponseEntity<Object> getRatings(@RequestBody Restaurant restaurant){

       return new ResponseEntity<> (reviewsRepository.getAllByRestaurantId(restaurant.getId()), HttpStatus.OK);
    }

    @RequestMapping(value = "/setRatings", method = RequestMethod.PUT)
    public ResponseEntity<Object> setRatings(@RequestBody Restaurant restaurant){
        Restaurant update = restaurantRepository.getRestaurantById(restaurant.getId());
        update.setRatings(restaurant.getRatings());
        restaurantRepository.save(update);

        return new ResponseEntity<> (reviewsRepository.getAllByRestaurantId(restaurant.getId()), HttpStatus.OK);
    }

}