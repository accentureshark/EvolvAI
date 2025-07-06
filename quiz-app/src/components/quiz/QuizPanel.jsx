import '../../styles/quiz.css'

import { useState } from "react";
import { CustomListBox } from "../ui/CustomListBox"
import { CustomButton } from "../ui/CustomButton"
import { QuizModal } from "../ui/QuizModal";

export const QuizPanel = () => {
const [value, setValue] = useState(
  [
    {
      "id": 1,
      "title": "Hola",
    },
    {
      "id": 2,
      "title": "Chao",
    },
    {
      "id": 3,
      "title": "Hola",
    },
    {
      "id": 4,
      "title": "Hola",
    },
    {
      "id": 5,
      "title": "Hola",
    },
    {
      "id": 6,
      "title": "Hola",
    },
    {
      "id": 7,
      "title": "Hola",
    },
    {
      "id": 8,
      "title": "Hola",
    },
    {
      "id": 9,
      "title": "Hola",
    },
    {
      "id": 10,
      "title": "Hola",
    },
  ]
  );
const [modalVisible, setModalVisible] = useState(false);


  return (
    <div className="quiz-panel-container">
      <div className="quiz-panel-header">
        <h4>Listado de Quiz</h4>
        <CustomButton
          icon="pi pi-file-plus"
          onClick={() => setModalVisible(true)}
        />
      </div>
      <CustomListBox className="quiz-panel-list" optionLabel={value => value.title} options={value} value={value => value.id} />
      <QuizModal visible={modalVisible} onHide={() => setModalVisible(false)} />
    </div>
  )
}
