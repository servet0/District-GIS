"""SQLAlchemy + GeoAlchemy2 model for the districts table."""

from sqlalchemy import Column, Integer, Float, Text
from sqlalchemy.orm import declarative_base
from geoalchemy2 import Geometry

Base = declarative_base()


class District(Base):
    __tablename__ = "districts"

    id = Column(Integer, primary_key=True, autoincrement=True)
    district_name = Column(Text, nullable=False)
    city_name = Column(Text, nullable=False)
    population = Column(Integer)
    income = Column(Float)
    education = Column(Float)
    infrastructure = Column(Float)
    employment = Column(Float)
    development_index = Column(Float)
    geom = Column(Geometry("MULTIPOLYGON", srid=4326))
