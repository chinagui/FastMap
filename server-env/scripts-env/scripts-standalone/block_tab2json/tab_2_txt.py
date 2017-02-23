# -*- coding: utf-8 -*
import sys
import os
import json
from osgeo import ogr,gdal

reload(sys)
sys.setdefaultencoding('utf-8')

def readTab(filename,outfile):

    outf = open(outfile,'w')
    try:
        #为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO")
        #为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING","")
        ogr.RegisterAll()
        driver = ogr.GetDriverByName("Mapinfo File")
        dataset = driver.Open(filename)
        if not dataset:
            print 'open file error'
            return
        layer = dataset.GetLayerByIndex(0)
        if not layer:
            print 'open layer error'
            return

        count=0
        total=0
        bulk=[]
        for feature in layer:
            province=feature.GetField('province').decode('gbk').encode('utf8')
            #if province!='北京市' and province!='上海市':
            #    continue

            city=feature.GetField('city').decode('gbk').encode('utf8')
            name=feature.GetField('Name').decode('gbk').encode('utf8')
            county=feature.GetField('County').decode('gbk').encode('utf8')
            area=feature.GetField('Area').decode('gbk').encode('utf8')
            job1=feature.GetField('Job1').decode('gbk').encode('utf8')
            job2=feature.GetField('Job2').decode('gbk').encode('utf8')
            code=feature.GetField('BlockCode').decode('gbk').encode('utf8')
            workProperty=feature.GetField('work_property').decode('gbk').encode('utf8')

            geom = feature.GetGeometryRef()
            if geom:
                try:
                    geometry=geom.ExportToWkt()
                except:
                    writeLog(logfile,'invalid geometry at index %d'%total)
                    continue
            else:
                writeLog(logfile,'invalid geometry at index %d'%total)
                continue

            total+=1
            count+=1

            data = {'province':province,'city':city,'name':name,'county':county,'area':area,'job1':job1,'job2':job2,'code':code,'workProperty':workProperty,'geometry':geometry}
            outf.write(json.dumps(data,ensure_ascii=False)+'\n')
            

    finally:
        if dataset:del dataset


if __name__ == '__main__':

    filename = sys.argv[1]
    outfilename = sys.argv[2]
    readTab(filename, outfilename)
