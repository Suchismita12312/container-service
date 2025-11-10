package com.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("bookings")
@CompoundIndexes({
        @CompoundIndex(name="by_origin_dest", def="{ 'origin': 1, 'destination': 1 }")
})
@Data
public class Booking {
    @Id private String bookingRef; // e.g. 957000001
    @Field("container_size") private int containerSize;
    @Field("container_type") private ContainerType containerType;
    @Field("origin")         private String origin;
    @Field("destination")    private String destination;
    @Field("quantity")       private int quantity;
    @Field("timestamp")      private String timestamp;
}