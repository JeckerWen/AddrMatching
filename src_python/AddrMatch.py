#!/usr/bin/env python
# encoding: utf-8
'''
@author: Sync
@license: (C) Copyright 2018
@contact: jeckerWen@gmail.com
@file: 地址相似度计算.py
@Date: 2019/1/30 10:59
@desc:
'''
import pandas as pd
import re
import pinyin
import os

SPLIT_LIST = ['省','市','县','镇','街道','村','自然村','小区','社区','区','里','弄','塘','乡']
DECAY_VALUE = 0.3


def to_pinyin(var_str):
    """
    汉字[钓鱼岛是中国的]=>拼音[diaoyudaoshizhongguode]\n
    汉字[我是shui]=>拼音[woshishui]\n
    汉字[AreYou好]=>拼音[AreYouhao]\n
    汉字[None]=>拼音[]\n
    汉字[]=>拼音[]\n
    :param var_str:  str 类型的字符串
    :return: 汉字转小写拼音
    """
    if isinstance(var_str, str):
        if var_str == 'None':
            return ""
        else:
            return pinyin.get(var_str, format='strip', delimiter="")
    elif isinstance(var_str, list):
        series = list(map(lambda x: pinyin.get(x, format='strip', delimiter=""), var_str))
        return series
    else:
        return -1


def dili_split(x):
    # 将一个地址字符串，按照地理实体定位词划分为地理实体的序列，并返回对应的拼音列表
    # SPLIT_LIST 定义了地理实体定位词
    global SPLIT_LIST
    try:
        res_dict = {}
        re_str = '|'.join(SPLIT_LIST)
        x = re.sub(r'[ ,\u3000,\xa0]+', u'', x)
        series_whole = re.split(u'(%s)' % re_str, x)
        series_no_split = re.split(u'%s' % re_str, x)
        if series_whole[-1] not in SPLIT_LIST:
            series_whole = series_whole[:-1]
            series_no_split = series_no_split[:-1]
        # 地理名词去重
        tuple_list = []
        quchong_list = []
        for i in range(0, len(series_whole), 2):
            tuple_list.append((series_whole[i], series_whole[i + 1]))
        tuple_list.reverse( )
        for p in tuple_list:
            if p not in quchong_list:
                quchong_list.append(p)
        quchong_list.reverse( )
        quchong_series = []
        quchong_no_split = []
        for i, p in enumerate(quchong_list):
            quchong_series.extend(p)
            quchong_no_split.append(p[0])
        res_dict['whole_series'] = series_whole
        res_dict['brief_series'] = series_no_split
        res_dict['brief_pinyin'] = to_pinyin(series_no_split)
        res_dict['whole_pinyin'] = to_pinyin(series_whole)
        res_dict['whole_quchong_series'] = quchong_series
        res_dict['brief_quchong_series'] = quchong_no_split
        res_dict['whole_quchong_pinyin'] = to_pinyin(quchong_series)
        res_dict['brief_quchong_pinyin'] = to_pinyin(quchong_no_split)
        res_dict['raw_series'] = x
        #         print(series_whole)
        #         print(series_no_split)
        return res_dict
    except:
        # 如果地址为空 返回空字典
        return {}


def com_similarity(addr, group):
    global DECAY_VALUE
    # 按照main_d 中的地理实体计算相似度
    #main_d 群
    #sub_d 地址
    main_d = dili_split(group)
    sub_d = dili_split(addr)
    #
    if len(main_d) == 0 or len(sub_d) == 0:
        return 0
    main_s = main_d['brief_quchong_pinyin']
    whole_main_s = main_d['whole_quchong_series']
    whole_sub_s = sub_d['whole_series']
    sub_s = set(sub_d['brief_pinyin'])
    total_sum = 0
    match_sum = 0
    match_level_list = []
    mul_index = 0
    match_words = []
    for index in range(len(main_s) - 1, -1, -1):
        cell = main_s[index]
        #         print(cell)
        level = whole_main_s[2 * index + 1]
        total_sum += DECAY_VALUE ** mul_index
        if cell in sub_s:
            match_words.append(cell);
            match_sum += DECAY_VALUE ** mul_index
            match_level_list.append(level)
        #             print(cell +'in')
        mul_index += 1
    if total_sum == 0:
        print('main', main_d['raw_series'])
        print('sub', sub_d['raw_series'])
        return 0
    #     return [match_sum/total_sum,tuple(match_level_list)]
    return match_words, match_sum / total_sum

##main
addr = '浙江省长兴县雉城街道申兴小区8-2-202至'
group = '长兴县雉城镇长兴小额零散客户群'
match_words, value = com_similarity(addr, group)
print(match_words, value)


