# jc_langtool
项目：简单文档翻译工具
简介：基于maven库的简单java web工具，用来实现导入词库，完成上传文件的部分翻译工作
功能：beta1.0 目前实现了批量上传单词表（excel格式），批量翻译文件（excel格式，计划支持word格式）
使用：
    1、单词表格式：1、3、5...单数列为词源，2、4、6...偶数列为相应词源的翻译
    2、翻译文件格式：手动添加第一行，标记需要进行翻译的列，在列头标记“lang”记号，例如，A列内容需要翻译到B列，则在A列列头写lang:B，
    如果只写lang默认翻译到下一列。
说明：
    界面基于开源jquery组件：https://blueimp.github.io/jQuery-File-Upload/
    
