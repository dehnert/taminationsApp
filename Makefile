
#  This file generates the xml files in the raw directory from
#  the corresponding files from the Taminations project.
#  Requires $(TAMINATIONS) to be set to the location of that project

OBJDIR = assets
SRC = $(wildcard $(TAMINATIONS)/*/*.xml) \
      $(wildcard $(TAMINATIONS)/*/*.html) \
      $(wildcard $(TAMINATIONS)/*/*.png)

SRCNAMES = $(subst $(TAMINATIONS),,$(SRC))
OBJ = $(addprefix $(OBJDIR),$(SRCNAMES))

all : $(OBJ)

$(OBJ) : $(SRC)

%.xml :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
#  The mobile site requires a viewport tag, but that breaks
#  scrolling for Gingerbread on the app.  So remove the viewport tag here.
%.html :
	perl -p -e "s/.*viewport.*//" $(subst $(OBJDIR),$(TAMINATIONS),$@) >$@
%.png :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
