package com.example.demo.entity;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "data")
@Getter
@Setter
public class Data {
	//這種策略是在使用像 MySQL 這樣的數據庫時常見的設置，它可以確保每個實體都有一個唯一的標識符。
    //當它與 @Id 註解一起使用時，表示該字段是實體的唯一標識符，並且其值將由數據庫自動生成。
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String data;

    // getters and setters
}
