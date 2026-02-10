package com.spartaifive.commercepayment.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;

    @Setter
    @Column(unique = true, nullable = false)
    private String customerUid;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "membership_id")
//    private MembershipGrade membershipGrade;

    private Long total_point = 0L;
    private Long total_paid_amount = 0L;

    public User(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }


}
