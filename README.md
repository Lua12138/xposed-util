# What's this

An Android Library that May Improve Xposd Module Development Efficiency

# How to use

- Import this project into your Android project
- Create a class named `com.github.gam2046.xposed.EntryPoint` and implement interface `com.github.gam2046.xposed.base.EntryConfiguration`.
  + Returns the object you need to inject in `com.github.gam2046.xposed.EntryPoint.entries(): Array<BaseEntry>`