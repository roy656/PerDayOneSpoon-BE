package com.sparta.perdayonespoon.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sparta.perdayonespoon.domain.dto.request.StatusDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name = "member")
@Entity // RDS
@ApiModel
public class Member extends  Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(example = "소셜 로그인한 사용자의 이메일 , 없을수도 있음")
    @Column
    private String Email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @ApiModelProperty(example = "소셜 로그인에서 사용하는 유저의 닉네임")
    @Column(nullable = false)
    private String nickname;

    @ApiModelProperty(example = "소셜로그인시 발급되는 소셜 ID")
    @Column(nullable = false, unique = true)
    private String socialId;

    @ApiModelProperty(example = "유저를 구분하기 위해 생성한 소셜 코드")
    private String socialCode;

    @ApiModelProperty(example = "회원가입한 사용자의 권한")
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @ApiModelProperty(example = "사용자가 사용하는 상태메세지")
    @Column
    private String status;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Image image;

    @Builder
    public Member(String email, String password,String socialCode, String nickname, String socialId, Authority authority,Image image) {
        this.Email = email;
        this.password = password;
        this.socialCode = socialCode;
        this.nickname = nickname;
        this.socialId = socialId;
        this.authority = authority;
        this.image = image;
    }

    public void SetTwoColumn(StatusDto statusDto){
        this.nickname = statusDto.getNickname();
        this.status = statusDto.getStatus();
    }

    public void Setname(String nickname){
        this.nickname = nickname;
    }
    public void SetStatus(String status){
        this.status = status;
    }

    public void SetImage(Image image){ this.image = image;}
}

