package com.company.decentralized_disaster_relief.auth_service.exceptions;

public class BadRequestException extends RuntimeException{

  public BadRequestException(String message){
      super(message);
  }
}
