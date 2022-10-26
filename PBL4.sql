create database FTP;
use FTP;

create table Files (
    FID int primary key auto_increment,
    ParentID int null,
    Name varchar(1000) not null,
    Type varchar(255) not null,
    Path varchar(1000) not null,
    Size bigint not null,
    CreatedDate datetime not null,
    LastEditedDate datetime null,
    Owner varchar(255) not null,
    LastEditedBy varchar(255) null,
    Permission int
);

create table User (
    UID int primary key auto_increment,
    UserName varchar(255) not null,
    FullName varchar(255) not null,
    Email varchar(255) null,
    Phone varchar(255) null,
    Password varchar(1000) not null,
    Role varchar(255) not null
);

create table `Group` (
    GID int primary key auto_increment,
    GroupName varchar(255) not null,
    Owner varchar(255) not null
);

create table User_Group (
    UID int not null,
    GID int not null,
    constraint `User_Group_User_FK` foreign key User_Group(UID) references User(UID),
    constraint `User_Group_Group_FK` foreign key User_Group(GID) references `Group`(GID)
);

create table Share (
    FID int,
    UID int,
    Permission int,
    constraint `Share_User_FK` foreign key Share(UID) references User(UID),
    constraint `Share_File_FK` foreign key Share(FID) references Files(FID)
);

insert into User(UserName, FullName, Email, Phone, Password, Role) values('admin1', 'Phuc Phan', 'pp311@gmail.com', '0875124512', '123', 'admin');
insert into User(UserName, FullName, Email, Phone, Password, Role) values('user1', 'May', 'may@gmail.com', '0875124421', '123', 'user');

