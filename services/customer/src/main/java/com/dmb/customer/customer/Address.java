package com.dmb.customer.customer;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Validated
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String houseNumber;
    private String zipCode;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private Customer customer;
}
