package com.narbase.kuntut

import com.narbase.kunafa.core.components.*
import com.narbase.kunafa.core.components.layout.LinearLayout
import com.narbase.kunafa.core.css.*
import com.narbase.kunafa.core.dimensions.dependent.matchParent
import com.narbase.kunafa.core.dimensions.dependent.weightOf
import com.narbase.kunafa.core.dimensions.dependent.wrapContent
import com.narbase.kunafa.core.dimensions.px
import com.narbase.kunafa.core.drawable.Color
import com.narbase.kunafa.core.lifecycle.LifecycleOwner
import com.narbase.kunafa.core.lifecycle.Observable
import org.w3c.dom.events.KeyboardEvent

/**
 * NARBASE TECHNOLOGIES CONFIDENTIAL
 * ______________________________
 * [2017] -[2019] Narbase Technologies
 * All Rights Reserved.
 * Created by islam
 * On: 2019/03/05.
 */

fun main() {
    page {
        mount(TodoComponent(TodoViewModel()))
    }
}

class TodoComponent(private val viewModel: TodoViewModel) : Component() {

    private val todoViews = mutableMapOf<Int, TodoItem>()

    private var listLayout: LinearLayout? = null
    private var todoTextInput: TextInput? = null


    override fun onViewCreated(lifecycleOwner: LifecycleOwner) {
        viewModel.onItemAdded.observe(::addItem)
        viewModel.onItemDeleted.observe(::deleteItem)
        viewModel.onItemUpdated.observe(::updateItem)
    }

    private fun addItem(pm: TodoPm?) {
        pm ?: return
        val component = TodoItem(pm, viewModel::deleteItem, viewModel::toggleItemStatus)
        listLayout?.mount(component)
        todoViews[pm.id] = component
    }

    private fun deleteItem(pm: TodoPm?) {
        pm ?: return
        val component = todoViews[pm.id] ?: return
        listLayout?.unMount(component)
        todoViews.remove(pm.id)
    }

    private fun updateItem(pm: TodoPm?) {
        pm ?: return
        val component = todoViews[pm.id] ?: return
        if (pm.isDone) {
            component.markDone()
        } else {
            component.markUndone()
        }
    }

    override fun View?.getView() = horizontalLayout {
        style {
            width = matchParent
            height = matchParent
        }

        verticalLayout {
            style {
                width = weightOf(1)
                minWidth = 200.px
                height = matchParent
                backgroundColor = Color.white
                padding = 32.px
                alignItems = Alignment.Center
            }

            textView {
                text = "Kunafa Todo"
                style {
                    fontSize = 32.px
                    color = Color(100, 240, 100)
                }
            }

            todoTextInput = textInput {
                style {
                    width = matchParent
                    backgroundColor = Color("#fafafa")
                    border = "1px solid #efefef"
                    padding = 8.px
                    borderRadius = 4.px
                    marginTop = 16.px
                }
                element.addEventListener("keypress", { e ->
                    if ((e as? KeyboardEvent)?.keyCode == 13) {
                        onButtonClicked()
                    }
                })
            }
            button {
                id = "myButton"
                text = "Add to do"
                style {
                    marginTop = 16.px
                }
                onClick = {
                    onButtonClicked()
                }
            }
        }

        verticalScrollLayout {
            style {
                width = weightOf(2)
                height = matchParent
                backgroundColor = Color("#ededed")
            }

            listLayout = verticalLayout {
                style {
                    width = matchParent
                    height = wrapContent
                    padding = 8.px
                }
            }
        }
    }

    private fun onButtonClicked() {
        viewModel.addNewTodo(todoTextInput?.text)
        todoTextInput?.text = ""
    }
}

class TodoViewModel {
    val onItemAdded = Observable<TodoPm>()
    val onItemDeleted = Observable<TodoPm>()
    val onItemUpdated = Observable<TodoPm>()

    private val todoItemsList = mutableListOf<TodoPm>()

    fun addNewTodo(todoText: String?) {
        if (todoText.isNullOrBlank()) return
        val pm = TodoPm(todoText)
        todoItemsList.add(pm)
        onItemAdded.value = pm
    }

    fun deleteItem(id: Int) {
        val item = todoItemsList.find { it.id == id } ?: return
        onItemDeleted.value = item
        todoItemsList.remove(item)
    }

    fun toggleItemStatus(id: Int) {
        val item = todoItemsList.find { it.id == id } ?: return
        item.isDone = item.isDone.not()
        onItemUpdated.value = item
    }
}

data class TodoPm(val text: String, var isDone: Boolean = false) {
    val id: Int = nextId

    companion object {
        private var nextId = 0
            get() = field++
    }
}

class TodoItem(
    private val todoPm: TodoPm,
    private val onDeleteClicked: (id: Int) -> Unit,
    private val onToggleState: (id: Int) -> Unit
) : Component() {

    private var checkboxView: View? = null
    private var todoTextView: TextView? = null
    override fun View?.getView() = horizontalLayout {
        addRuleSet(Style.rootLayout)
        onClick = { onToggleState(todoPm.id) }

        checkboxView = view {
            addRuleSet(Style.circleBasic)
        }
        todoTextView = textView {
            style {
                width = weightOf(1)
                fontSize = 16.px
            }
            text = todoPm.text
        }
        button {
            text = "Delete"
            onClick = { onDeleteClicked(todoPm.id) }
            addRuleSet(Style.deleteButtonStyle)
        }
    }

    fun markDone() {
        checkboxView?.addRuleSet(Style.circleDone)
        todoTextView?.addRuleSet(Style.textDone)
    }

    fun markUndone() {
        checkboxView?.removeRuleSet(Style.circleDone)
        todoTextView?.removeRuleSet(Style.textDone)
    }

    companion object {
        object Style {
            val circleBasic = classRuleSet {
                width = 8.px
                height = 8.px
                borderRadius = 8.px
                border = "1px solid #888"
                marginRight = 8.px
            }
            val circleDone = classRuleSet {
                backgroundColor = Color(100, 240, 100)
                border = "1px solid ${Color(100, 240, 100).toCss()}"
            }
            val textDone = classRuleSet {
                textDecoration = "line-through"
                color = Color("#ccc")
            }
            val deleteButtonStyle = classRuleSet {
                borderRadius = 4.px
                backgroundColor = Color(230, 100, 100)
                color = Color.white
                padding = 4.px
                border = "none"
                cursor = "pointer"
                hover {
                    backgroundColor = Color(240, 40, 40)
                }
            }
            val rootLayout = classRuleSet {
                width = matchParent
                border = "1px solid #d4d4d4"
                marginTop = 8.px
                padding = 8.px
                alignItems = Alignment.Center
                cursor = "pointer"
                backgroundColor = Color.white
                hover {
                    boxShadow = "0px 4px 3px #bbb"
                }
            }
        }
    }
}



