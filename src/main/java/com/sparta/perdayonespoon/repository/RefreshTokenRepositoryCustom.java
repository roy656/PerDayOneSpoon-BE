package com.sparta.perdayonespoon.repository;

import com.sparta.perdayonespoon.domain.Member;
import com.sparta.perdayonespoon.domain.dto.request.TokenSearchCondition;
import com.sparta.perdayonespoon.domain.dto.response.MemberResponseDto;
import com.sparta.perdayonespoon.domain.dto.response.TwoFieldDto;
import com.sparta.perdayonespoon.jwt.Principaldetail;

import java.util.List;

public interface RefreshTokenRepositoryCustom {

    TwoFieldDto getMember(TokenSearchCondition condition);
}
