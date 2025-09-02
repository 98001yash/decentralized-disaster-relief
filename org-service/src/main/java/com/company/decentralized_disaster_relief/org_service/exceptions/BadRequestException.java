package com.company.decentralized_disaster_relief.org_service.exceptions;

public class BadRequestException extends RuntimeException{

  public BadRequestException(String message){
      super(message);
  }
}
